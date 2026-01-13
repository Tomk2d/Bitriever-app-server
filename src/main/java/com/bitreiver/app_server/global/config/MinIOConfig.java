package com.bitreiver.app_server.global.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinIOConfig {
    @Value("${minio.endpoint}")
    private String endpoint;
    
    @Value("${minio.access-key}")
    private String accessKey;
    
    @Value("${minio.secret-key}")
    private String secretKey;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Bean
    public MinioClient minioClient() {
        try {
            log.info("MinIO 클라이언트 초기화 시작 - endpoint: {}, accessKey: {}", endpoint, accessKey);
            
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            
            // 버킷 존재 확인 및 생성
            boolean found = client.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            
            if (!found) {
                client.makeBucket(
                        io.minio.MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("MinIO 버킷 생성 완료: {}", bucketName);
            } else {
                log.info("MinIO 버킷 이미 존재: {}", bucketName);
            }
            
            log.info("MinIO 클라이언트 초기화 성공");
            return client;
        } catch (io.minio.errors.ErrorResponseException e) {
            log.error("MinIO 클라이언트 초기화 실패 - ErrorResponse: {}", e.getMessage());
            log.error("MinIO 설정 확인 필요 - endpoint: {}, accessKey: {}, bucketName: {}", endpoint, accessKey, bucketName);
            throw new RuntimeException("MinIO 초기화 실패: " + e.getMessage() + 
                    ". MinIO 서버가 실행 중인지, 액세스 키가 올바른지 확인하세요.", e);
        } catch (Exception e) {
            log.error("MinIO 클라이언트 초기화 실패", e);
            log.error("MinIO 설정 - endpoint: {}, accessKey: {}, bucketName: {}", endpoint, accessKey, bucketName);
            throw new RuntimeException("MinIO 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    @Bean
    public String minioBucketName() {
        return bucketName;
    }
}
