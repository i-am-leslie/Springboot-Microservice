package com.example.gatewayserver.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class Prefilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID = "correlation-id";

    private static  final int FILTER_ORDER=0;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Tracking filter invoked...");

//        Gets the request made to the api gateway and stores it in a variable to get the header
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders header = request.getHeaders();
        if(hasCorrelationId(header)){
            log.info("Tracked request with correlation id %s {}", header.get(CORRELATION_ID));
        }else{
            log.info("Attaching correlation id to the header");
            request = exchange.getRequest()
                    .mutate()
                    .header(CORRELATION_ID, generateCorrelationId())
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }

    private boolean hasCorrelationId(HttpHeaders header){
        return header.containsKey(CORRELATION_ID);
    }

    private String generateCorrelationId(){
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }

}

