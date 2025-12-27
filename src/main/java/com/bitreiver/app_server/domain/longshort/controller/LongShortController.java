package com.bitreiver.app_server.domain.longshort.controller;

import com.bitreiver.app_server.domain.longshort.dto.LongShortResponse;
import com.bitreiver.app_server.domain.longshort.service.LongShortService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/longshort")
@RequiredArgsConstructor
@Tag(name = "LongShort", description = "롱숏 비율 조회 API")
public class LongShortController {

    private final LongShortService longShortService;

    @Operation(
        summary = "롱숏 비율 조회",
        description = "symbol 과 period 에 해당하는 롱숏 비율 시계열 데이터를 조회합니다. " +
                      "데이터는 fetch-server 에서 주기적으로 Binance API 를 호출해 Redis 에 저장한 값을 사용합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터"
        )
    })
    @GetMapping
    public ApiResponse<List<LongShortResponse>> getLongShortRatio(
        @Parameter(
            name = "symbol",
            description = "코인 심볼 (예: BTC, ETH)",
            example = "BTC",
            required = true,
            in = ParameterIn.QUERY
        )
        @RequestParam("symbol") String symbol,

        @Parameter(
            name = "period",
            description = "기간 (1h, 4h, 12h, 1d)",
            example = "1h",
            required = true,
            in = ParameterIn.QUERY
        )
        @RequestParam("period") String period
    ) {

        List<LongShortResponse> responses = longShortService.getLongShortRatio(symbol, period);
        return ApiResponse.success(responses);
    }
}

