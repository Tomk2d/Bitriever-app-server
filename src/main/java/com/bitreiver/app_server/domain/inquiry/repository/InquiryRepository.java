package com.bitreiver.app_server.domain.inquiry.repository;

import com.bitreiver.app_server.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Inquiry> findByIdAndUserId(Long id, UUID userId);
}
