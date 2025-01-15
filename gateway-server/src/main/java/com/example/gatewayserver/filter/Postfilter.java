package com.example.gatewayserver.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
public class Postfilter implements GlobalFilter, Ordered {
    private static  final int FILTER_ORDER=1;
    public static final String CORRELATION_ID = "correlation-id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Injecting correlation into response...");


        HttpHeaders header = exchange.getRequest().getHeaders();


        exchange.getResponse().getHeaders().add(CORRELATION_ID, getCorrelationId(header));

        return chain.filter(exchange);
    }

    private String getCorrelationId(HttpHeaders header) {
        return header.getFirst(CORRELATION_ID) != null
                ? header.getFirst(CORRELATION_ID)
                : "default-correlation-id"; // Default or generate a unique ID
    }

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}
