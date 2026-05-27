package br.ufpb.dcx.apps4society.educapi.services;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

@Service
public class UploadImageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public UploadImageService(
            @Value("${minio.url}") String url,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey
    ) {
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String uploadFile(String folder, String fileName, InputStream inputStream, long size, String contentType) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
            }

            String safeFileName
                    = folder + "/"
                    + System.currentTimeMillis()
                    + "_"
                    + fileName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(safeFileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );

            return "http://localhost:9000/" + bucket + "/" + safeFileName;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload para o MinIO", e);
        }
    }

    public void deleteFileByUrl(String imageUrl) {
        Optional<String> objectName = extractObjectNameFromUrl(imageUrl);
        if (objectName.isEmpty()) {
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName.get())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover arquivo do MinIO", e);
        }
    }

    public byte[] getFileBytesByUrl(String imageUrl) {
        String objectName = extractObjectNameFromUrl(imageUrl)
                .orElseThrow(() -> new RuntimeException("URL de imagem invalida para o bucket configurado"));

        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        )) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar arquivo do MinIO", e);
        }
    }

    private Optional<String> extractObjectNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return Optional.empty();
        }

        String bucketMarker = "/" + bucket + "/";
        int bucketIndex = imageUrl.indexOf(bucketMarker);
        if (bucketIndex < 0) {
            return Optional.empty();
        }

        String objectName = imageUrl.substring(bucketIndex + bucketMarker.length());
        if (objectName.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(objectName);
    }

    public String generateBase64Thumbnail(MultipartFile file) {
        try {
            return generateBase64Thumbnail(file.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar thumbnail Base64", e);
        }
    }

    public String generateBase64Thumbnail(byte[] imageBytes) {
        try {
            BufferedImage originalImage = ImageIO.read(
                    new java.io.ByteArrayInputStream(imageBytes)
            );

            if (originalImage == null) {
                throw new RuntimeException("Não foi possível ler a imagem enviada para gerar thumbnail");
            }

            int width = 200;
            int height = 200;

            Image scaledImage = originalImage.getScaledInstance(
                    width,
                    height,
                    Image.SCALE_SMOOTH
            );

            BufferedImage thumbnail = new BufferedImage(
                    width,
                    height,
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = thumbnail.createGraphics();
            graphics.drawImage(scaledImage, 0, 0, null);
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar thumbnail Base64", e);
        }
    }
}
