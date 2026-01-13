package com.bitreiver.app_server.domain.diary.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface DiaryImageService {
    String uploadImage(Integer diaryId, MultipartFile file);
    Resource downloadImage(Integer diaryId, String imagePath);
    void deleteImage(Integer diaryId, String imagePath);
    void deleteAllImages(Integer diaryId, java.util.List<String> imagePaths);
}
