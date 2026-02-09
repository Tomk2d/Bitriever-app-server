package com.bitreiver.app_server.domain.inquiry.service;

import com.bitreiver.app_server.domain.inquiry.dto.InquiryResponse;
import com.bitreiver.app_server.domain.inquiry.entity.Inquiry;
import com.bitreiver.app_server.domain.inquiry.enums.InquiryStatus;
import com.bitreiver.app_server.domain.inquiry.repository.InquiryRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    @Override
    @Transactional
    public InquiryResponse createInquiry(UUID userId, String content) {
        Inquiry inquiry = Inquiry.builder()
            .userId(userId)
            .content(content)
            .status(InquiryStatus.NEW)
            .build();
        Inquiry saved = inquiryRepository.save(inquiry);
        return InquiryResponse.from(saved);
    }

    @Override
    public List<InquiryResponse> getMyInquiries(UUID userId) {
        List<Inquiry> inquiries = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return inquiries.stream()
            .map(InquiryResponse::from)
            .toList();
    }

    @Override
    public InquiryResponse getInquiry(UUID userId, Long id) {
        Inquiry inquiry = inquiryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));
        return InquiryResponse.from(inquiry);
    }
}
