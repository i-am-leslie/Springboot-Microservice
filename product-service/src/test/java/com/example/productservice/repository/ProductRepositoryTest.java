package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;


    @AfterEach
    void tearDown(){
        productRepository.deleteAll();
    }
    @Test
    void findByName() {

    }

    @Test
    void findIdByName() {
        Product sampleProduct= new Product("12345","Fan","Sample product","10",10);
        productRepository.save(sampleProduct);
        //when
        String productId= productRepository.findIdByName("Fan");
        //then
        assertEquals(productId,"12345");
    }

    @Test
    void findIdByNameDoesNotExist() {
        //Given
        Product sampleProduct= new Product("12345","Fan","Sample product","10",10);
        productRepository.save(sampleProduct);
        //When
        String result = productRepository.findIdByName("bag");
        //Then
        assertNull(result);
    }
}