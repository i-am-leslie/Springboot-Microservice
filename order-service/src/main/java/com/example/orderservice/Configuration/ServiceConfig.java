package com.example.orderservice.Configuration;



import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ServiceConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;


    @PostConstruct
    public void init() {
        System.out.println("Redis Host: " + redisHost);  // Log the values
        System.out.println("Redis Port: " + redisPort);  // Log the values
    }
    public String getRedisHost() {
        return redisHost;
    }

    public String getRedisPort() {
        return redisPort;
    }

}
