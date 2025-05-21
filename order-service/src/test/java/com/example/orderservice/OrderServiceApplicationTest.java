package com.example.orderservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;

@TestConfiguration(proxyBeanMethods = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = {OrderServiceApplication.class, TestContainersConfig.class} )
class OrderServiceApplicationTest {


    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
    }

    @Test
    void contextLoads() {}


}