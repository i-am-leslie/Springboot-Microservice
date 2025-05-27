package com.example.orderservice;

import com.example.orderservice.feign.FeignClient;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
        return new GenericContainer<>("redis:7.2").withExposedPorts(6379).withReuse(true).withCreateContainerCmdModifier(cmd ->
                cmd.withHostConfig(
                        cmd.getHostConfig().withPortBindings(
                                new PortBinding(Ports.Binding.bindPort(6379), new ExposedPort(6379))
                        )
                ));
    }

    @Bean
    public FeignClient feignClient() {
        return Mockito.mock(FeignClient.class);
    }
}
