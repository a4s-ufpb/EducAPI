package br.ufpb.dcx.apps4society.educapi.services;


import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;


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
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );


            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
            }

            String safeFileName = System.currentTimeMillis() + "_" + fileName;

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
}
