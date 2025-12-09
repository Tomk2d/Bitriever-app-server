package com.bitreiver.app_server.domain.feargreed.service;

import com.bitreiver.app_server.domain.feargreed.dto.FearGreedResponse;

import java.time.LocalDate;

public interface FearGreedService {
    FearGreedResponse getByDate(LocalDate date);
}

