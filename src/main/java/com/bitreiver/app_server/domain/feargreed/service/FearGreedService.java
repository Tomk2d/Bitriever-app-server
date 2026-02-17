package com.bitreiver.app_server.domain.feargreed.service;

import com.bitreiver.app_server.domain.feargreed.dto.FearGreedResponse;

import java.time.LocalDate;
import java.util.List;

public interface FearGreedService {
    FearGreedResponse getByDate(LocalDate date);
    FearGreedResponse getToday();
    List<FearGreedResponse> getAllHistory();
    List<FearGreedResponse> getByDateRange(LocalDate start, LocalDate end);
}

