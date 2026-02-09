package com.bitreiver.app_server.domain.inquiry.entity;

import com.bitreiver.app_server.domain.inquiry.enums.InquiryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inquiry", indexes = {
    @Index(name = "idx_inquiry_user_id", columnList = "user_id"),
    @Index(name = "idx_inquiry_status", columnList = "status")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.NEW;

    @Column(name = "admin_memo", columnDefinition = "TEXT")
    private String adminMemo;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "handled_at")
    private LocalDateTime handledAt;
}
