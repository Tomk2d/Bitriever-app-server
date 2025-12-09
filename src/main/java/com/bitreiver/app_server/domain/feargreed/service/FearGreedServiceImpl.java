package com.bitreiver.app_server.domain.feargreed.service;

import com.bitreiver.app_server.domain.feargreed.dto.FearGreedResponse;
import com.bitreiver.app_server.domain.feargreed.entity.FearGreedIndex;
import com.bitreiver.app_server.domain.feargreed.repository.FearGreedIndexRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FearGreedServiceImpl implements FearGreedService {
    
    private final FearGreedIndexRepository fearGreedIndexRepository;
    
    @Override
    public FearGreedResponse getByDate(LocalDate date) {
        FearGreedIndex index = fearGreedIndexRepository.findByDate(date)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        
        return FearGreedResponse.builder()
            .id(index.getId())
            .date(index.getDate())
            .value(index.getValue())
            .build();
    }
}
