package com.bitreiver.app_server.domain.price.controller;

import com.bitreiver.app_server.domain.price.dto.CoinTickerPriceDto;
import com.bitreiver.app_server.domain.price.service.CoinPriceTickerService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/coin-prices/ticker")
@RequiredArgsConstructor
@Tag(name = "Coin Price Ticker", description = "코인 현재가 조회 API")
public class CoinPriceTickerController {
    private final CoinPriceTickerService coinPriceTickerService;
    
    /**
     * 전체 코인 현재가 조회 (클라이언트 최초 연결 시 사용)
     * 서버에서 관리하는 모든 코인의 현재 가격을 반환
     * exchange 파라미터가 있으면 해당 거래소의 코인만 반환
     */
    @Operation(summary = "전체 코인 현재가 조회", description = "서버에서 관리하는 모든 코인의 현재 가격을 조회합니다. exchange 파라미터로 거래소별 필터링이 가능합니다. 클라이언트 최초 연결 시 사용합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/all")
    public ApiResponse<List<CoinTickerPriceDto>> getAllCoinPrices(
            @Parameter(description = "거래소 이름 (UPBIT, COINONE 등). 생략 시 모든 거래소의 코인을 반환합니다.")
            @RequestParam(value = "exchange", required = false) String exchange) {
        List<CoinTickerPriceDto> prices;
        if (exchange != null && !exchange.isEmpty()) {
            prices = coinPriceTickerService.getCoinPricesByExchange(exchange);
        } else {
            prices = coinPriceTickerService.getAllCoinPrices();
        }
        return ApiResponse.success(prices);
    }
}

