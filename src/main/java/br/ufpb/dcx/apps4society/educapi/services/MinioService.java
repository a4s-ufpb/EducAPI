package br.ufpb.dcx.apps4society.educapi.services;


import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.InputStream;


@Service
public class MinioService {


    private final MinioClient minioClient;


    @Value("${minio.bucket}")
    private String bucket;


    public MinioService(
            @Value("${minio.url}") String url,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey
    ) {
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }


    public String uploadFile(String fileName, InputStream inputStream, long size, String contentType) {
        try {
            // 🔹 Garante que o bucket existe
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );


            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
            }


            // 🔹 Evita sobrescrever arquivos
            String safeFileName = System.currentTimeMillis() + "_" + fileName;


            // 🔹 Upload correto com tamanho real
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(safeFileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );


            return safeFileName;


        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload para o MinIO", e);
        }
    }
}
