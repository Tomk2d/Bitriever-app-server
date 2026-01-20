package com.bitreiver.app_server.domain.community.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommunityImageService {
    String uploadImage(Integer communityId, MultipartFile file);
    Resource downloadImage(Integer communityId, String imagePath);
    void deleteImage(Integer communityId, String imagePath);
    void deleteAllImages(Integer communityId, List<String> imagePaths);
}
