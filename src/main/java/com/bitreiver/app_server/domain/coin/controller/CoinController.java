package com.bitreiver.app_server.domain.coin.controller;

import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
import com.bitreiver.app_server.domain.coin.service.CoinService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
@Tag(name = "Coin", description = "코인 정보 조회 API")
public class CoinController {
    
    private final CoinService coinService;
    
    @Operation(summary = "코인 목록 조회", description = "전체 코인 목록을 조회하거나 거래소별로 필터링하여 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<List<CoinResponse>> getAllCoins(@RequestParam(required = false) String exchange) {
        if (exchange != null) {
            log.info("거래소별 코인 조회 - exchange: {}", exchange);
            List<CoinResponse> coins = coinService.getCoinsByExchange(exchange);
            return ApiResponse.success(coins);
        }
        
        log.info("전체 코인 조회");
        List<CoinResponse> coins = coinService.getAllCoins();
        return ApiResponse.success(coins);
    }
    
    @Operation(summary = "코인 개별 조회", description = "코인 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코인을 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ApiResponse<CoinResponse> getCoinById(@PathVariable Integer id) {
        log.info("코인 조회 - id: {}", id);
        CoinResponse coin = coinService.getCoinById(id);
        return ApiResponse.success(coin);
    }
}

