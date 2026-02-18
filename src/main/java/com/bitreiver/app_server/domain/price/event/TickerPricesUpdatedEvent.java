package com.bitreiver.app_server.domain.price.event;

import org.springframework.context.ApplicationEvent;

/**
 * Ticker 수집이 완료되어 캐시가 갱신된 시점에 발행.
 * 당일 일봉 Redis 병합 등이 이 이벤트를 구독한다.
 */
public class TickerPricesUpdatedEvent extends ApplicationEvent {

    public TickerPricesUpdatedEvent(Object source) {
        super(source);
    }
}
