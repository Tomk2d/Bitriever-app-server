package com.bitreiver.app_server.domain.diary.service;

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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryImageServiceImpl implements DiaryImageService {
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${diary.image.max-size:5242880}") // 기본값 5MB
    private long maxFileSize;
    
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    
    @Override
    public String uploadImage(Integer diaryId, MultipartFile file) {
        // 파일 유효성 검증
        validateFile(file);
        
        // 파일명 생성: {diaryId}_{timestamp}.{확장자}
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = String.format("%d_%s.%s", diaryId, timestamp, extension);
        
        // 객체 키: diary-images/{diaryId}/{filename}
        String objectKey = String.format("diary-images/%d/%s", diaryId, filename);
        
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
            
            log.info("이미지 업로드 성공: diaryId={}, objectKey={}", diaryId, objectKey);
            
            // DB에 저장할 경로 형식: @diaryImage/{diaryId}/{filename}
            return String.format("@diaryImage/%d/%s", diaryId, filename);
            
        } catch (Exception e) {
            log.error("이미지 업로드 실패: diaryId={}, filename={}", diaryId, filename, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public Resource downloadImage(Integer diaryId, String imagePath) {
        // imagePath 형식: @diaryImage/{diaryId}/{filename}
        String filename = extractFilename(imagePath);
        String objectKey = String.format("diary-images/%d/%s", diaryId, filename);
        
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            
            return new InputStreamResource(stream);
            
        } catch (Exception e) {
            log.error("이미지 다운로드 실패: diaryId={}, objectKey={}", diaryId, objectKey, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 다운로드에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteImage(Integer diaryId, String imagePath) {
        String filename = extractFilename(imagePath);
        String objectKey = String.format("diary-images/%d/%s", diaryId, filename);
        
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            
            log.info("이미지 삭제 성공: diaryId={}, objectKey={}", diaryId, objectKey);
            
        } catch (Exception e) {
            log.error("이미지 삭제 실패: diaryId={}, objectKey={}", diaryId, objectKey, e);
            // 삭제 실패해도 예외를 던지지 않음 (이미 DB에서 제거된 경우)
        }
    }
    
    @Override
    public void deleteAllImages(Integer diaryId, List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }
        
        for (String imagePath : imagePaths) {
            deleteImage(diaryId, imagePath);
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
        // @diaryImage/{diaryId}/{filename} 형식에서 filename 추출
        if (imagePath == null || !imagePath.startsWith("@diaryImage/")) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 이미지 경로 형식입니다.");
        }
        
        String[] parts = imagePath.split("/");
        if (parts.length < 3) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 이미지 경로 형식입니다.");
        }
        
        return parts[2];
    }
}
