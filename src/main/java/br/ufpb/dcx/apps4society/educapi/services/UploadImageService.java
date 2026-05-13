package br.ufpb.dcx.apps4society.educapi.services;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

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

    public String generateBase64Thumbnail(MultipartFile file) {
        try {

            BufferedImage originalImage = ImageIO.read(file.getInputStream());

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

            System.out.println("IMAGE: " + originalImage);

            Graphics2D graphics = thumbnail.createGraphics();
            graphics.drawImage(scaledImage, 0, 0, null);
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ImageIO.write(thumbnail, "jpg", outputStream);

            byte[] imageBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar thumbnail Base64", e);
        }
    }
}
