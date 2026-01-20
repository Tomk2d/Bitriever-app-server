package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityImageServiceImpl implements CommunityImageService {
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${community.image.max-size:5242880}") // 기본값 5MB
    private long maxFileSize;
    
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    
    @Override
    public String uploadImage(Integer communityId, MultipartFile file) {
        // 파일 유효성 검증
        validateFile(file);
        
        // 파일명 생성: {communityId}_{timestamp}_{random}.{확장자}
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String filename = String.format("%d_%s_%s.%s", communityId, timestamp, randomSuffix, extension);
        
        // 객체 키: community-images/{communityId}/{filename}
        String objectKey = String.format("community-images/%d/%s", communityId, filename);
        
        try {
            // MinIO에 업로드
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );            
            // DB에 저장할 경로 형식: @communityImage/{communityId}/{filename}
            return String.format("@communityImage/%d/%s", communityId, filename);
            
        } catch (Exception e) {
            log.error("이미지 업로드 실패: communityId={}, filename={}", communityId, filename, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public Resource downloadImage(Integer communityId, String imagePath) {
        // imagePath 형식: @communityImage/{communityId}/{filename}
        String filename = extractFilename(imagePath);
        String objectKey = String.format("community-images/%d/%s", communityId, filename);
        
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            
            return new InputStreamResource(stream);
            
        } catch (Exception e) {
            log.error("이미지 다운로드 실패: communityId={}, objectKey={}", communityId, objectKey, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 다운로드에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteImage(Integer communityId, String imagePath) {
        String filename = extractFilename(imagePath);
        String objectKey = String.format("community-images/%d/%s", communityId, filename);
        
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );            
        } catch (Exception e) {
            log.error("이미지 삭제 실패: communityId={}, objectKey={}", communityId, objectKey, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteAllImages(Integer communityId, List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }
        
        for (String imagePath : imagePaths) {
            deleteImage(communityId, imagePath);
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "파일이 비어있습니다.");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new CustomException(ErrorCode.BAD_REQUEST, 
                    String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / 1024 / 1024));
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, 
                    "지원하는 이미지 형식은 JPEG, PNG, GIF, WEBP입니다.");
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg"; // 기본 확장자
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private String extractFilename(String imagePath) {
        // @communityImage/{communityId}/{filename} 형식에서 filename 추출
        if (imagePath == null || !imagePath.startsWith("@communityImage/")) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 이미지 경로 형식입니다.");
        }
        
        String[] parts = imagePath.split("/");
        if (parts.length < 3) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 이미지 경로 형식입니다.");
        }
        
        return parts[2];
    }
}
