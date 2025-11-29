package com.bitreiver.app_server.domain.price.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayResponse;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayRangeRequest;
import com.bitreiver.app_server.domain.price.service.CoinPriceDayService;
import com.bitreiver.app_server.global.common.response.ApiResponse;

import java.util.List;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/coin-prices/day")
@RequiredArgsConstructor
@Tag(name = "Coin Prices Day", description = "코인 가격 일봉 조회 API")
public class CoinPriceDayController {
    private final CoinPriceDayService coinPriceDayService;
    
    @Operation(summary = "(test) id로 코인 가격 일별 조회", description = "코인 가격 일별 조회를 합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코인 가격 일별 조회를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ApiResponse<CoinPriceDayResponse> getCoinPriceDayById(
        @Parameter(name = "id", description = "코인 가격 일봉 ID", example = "707460", required = true, in = ParameterIn.PATH)
        @PathVariable("id") Integer id
    ) {
        CoinPriceDayResponse response = coinPriceDayService.getCoinPriceDayById(id);
        return ApiResponse.success(response);
    }

    @Operation(summary = "코인 전체 날짜 일봉 조회", description = "개별 코인의 전체날짜 일봉 데이터를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공. 데이터 없으면 빈 배열 반환 {}"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코인 가격 일별 조회를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}/all")
    public ApiResponse<List<CoinPriceDayResponse>> getCoinPriceDayAllById(
        @Parameter(name = "id", description = "코인 ID", example = "1", required = true, in = ParameterIn.PATH)
        @PathVariable("id") Integer id
    ) {
        List<CoinPriceDayResponse> response = coinPriceDayService.getCoinPriceDayAllById(id);
        return ApiResponse.success(response);
    }

    @Operation(summary = "코인 기간별 일봉 조회", description = "개별 코인의 전체날짜 일봉 데이터를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공. 데이터 없으면 빈 배열 반환 {}"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코인 가격 일별 조회를 찾을 수 없습니다.")
    })
    @PostMapping("/range")
    public ApiResponse<List<CoinPriceDayResponse>> getCoinPriceDayRangeById(@Valid @RequestBody CoinPriceDayRangeRequest request) {
        List<CoinPriceDayResponse> response = coinPriceDayService.getCoinPriceDayRangeById(request);
        return ApiResponse.success(response);
    }
}
