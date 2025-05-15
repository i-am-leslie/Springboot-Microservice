package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        assertEquals("12345", productId);
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

//    @Test
//    void deleteProductById() {
//        //Given
//        Product sampleProduct= new Product("12345","Fan","Sample product","10",10);
//        productRepository.save(sampleProduct);
//
//
//        //When
//        int result= productRepository.deleteProductById("12345");
//
//        //Then
//        assertEquals(1, result);
//    }
//
//    @Test
//    void deleteProductByIdNotExist() {
//        //Given
//        Product sampleProduct= new Product("12345","Fan","Sample product","10",10);
//        productRepository.save(sampleProduct);
//
//
//        //When
//        int result= productRepository.deleteProductById("1234");
//
//        //Then
//        assertEquals(0, result);
//    }
}