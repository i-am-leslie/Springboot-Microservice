package com.example.productservice.service;

import com.example.productservice.DTO.ProductRequestDTO;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StreamBridge streamBridge;
    @Mock
    private ProductFuzzySearch productFuzzySearch;


    private ProductService productService;
    private AutoCloseable closeable;


    @BeforeEach
    void setUp(){
        closeable =MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository,streamBridge,productFuzzySearch);

    }



//    @Test
//    void saveProduct() {
//
//
//    }

    @Test
    void findProductByName() {

    }

    @Test
    void deleteProduct() {
    }

    @Test
    void testCreateProduct() {
        // Given
        ProductRequestDTO dto = new ProductRequestDTO(
                "Fan",                // name
                "Description",                    // Description
                "10",       // quantityInStock
                10                  // price
        );

        //When
        productService.createProduct(dto);

        // Then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertEquals("Fan", savedProduct.getName());
        assertEquals("Description", savedProduct.getProductDescription());
        assertEquals("10", savedProduct.getQuantityInStock());
        assertEquals(10, savedProduct.getPrice());
    }

    @Test
    void sendToOrderService() {
    }

    @Test
    void getAllProducts() {
    }

    @Test
    void getProductById() {
        // Given
        String id = "123";
        productRepository.save(new Product("123", "Fan", "Test", "10",10));
        when(productRepository.existsById(id)).thenReturn(true);

        //When
        boolean exists = productService.getProductById(id);

        //Then
        assertTrue(exists);
        verify(productRepository).existsById(id);
    }

    @Test
    void fallbackMethod() {
    }

    @Test
    void fallbackDeleteMethod() {
    }

    @Test
    void fallbackSendToOrderService() {
    }
}