package com.example.orderservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {
    @Bean
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16.0");
    }

    @Bean
    @ServiceConnection(name = "redis") // Optional: name it if you want
    static GenericContainer<?> redisContainer() {
        return new GenericContainer<>("redis:7.2").withExposedPorts(6379);
    }
}
