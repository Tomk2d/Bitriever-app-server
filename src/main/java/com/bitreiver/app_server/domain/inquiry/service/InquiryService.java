package com.bitreiver.app_server.domain.inquiry.service;

import com.bitreiver.app_server.domain.inquiry.dto.InquiryResponse;

import java.util.List;
import java.util.UUID;

public interface InquiryService {

    InquiryResponse createInquiry(UUID userId, String content);

    List<InquiryResponse> getMyInquiries(UUID userId);

    InquiryResponse getInquiry(UUID userId, Long id);
}
