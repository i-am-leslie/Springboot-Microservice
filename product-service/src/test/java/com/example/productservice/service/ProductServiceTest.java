package com.example.productservice.service;

import com.example.productservice.DTO.ProductDTO;
import com.example.productservice.DTO.ProductDetails;
import com.example.productservice.DTO.ProductEvent;
import com.example.productservice.DTO.ProductRequestDTO;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StreamBridge streamBridge;
    @Mock
    private ProductFuzzySearch productFuzzySearch;


    private ProductService productService;



    @BeforeEach
    void setUp(){
        productService = new ProductService(productRepository,streamBridge,productFuzzySearch);
    }



//    @Test
//    void saveProduct() {
//
//
//    }

    @Test
    void findProductByName() {
        //Given
        String productName="Fan";
        Product product1= new Product("123","Fan","Test product","1",1);
        productRepository.save(product1);


        //When
        when(productFuzzySearch.searchProducts(productName)).thenReturn(product1);
        Product expectedProduct=productService.findProductByName(productName);


        //Then
        assertEquals(product1,expectedProduct);
        verify(productFuzzySearch).searchProducts(productName);
    }

    @Test
    void deleteProduct() {
        // Given
        String id = "123";
        Product product1 = new Product("123", "Fan", "Test product", "1", 1);

        when(productRepository.findById(id)).thenReturn(Optional.of(product1));
        doNothing().when(productRepository).deleteById(id);

        // When
        boolean result = productService.deleteProduct(id);

        // Then
        verify(productRepository).deleteById(id);
        assertTrue(result);

        ArgumentCaptor<Message<ProductEvent>> productEventCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge).send(eq("productSupplier-out-0"), productEventCaptor.capture());

        Message<ProductEvent> sentMessage = productEventCaptor.getValue();
        ProductEvent event = sentMessage.getPayload();

        assertEquals("DELETED", event.getAction());
        assertEquals("123", event.getPrimaryId());
    }
//
//    @Test
//    void deleteNoProduct() {
//        //Given
//        String id="1234";
//        Product product1= new Product("123","Fan","Test product","1",1);
//        productRepository.save(product1);
//        when(productRepository.deleteProductById(id)).thenReturn(0);
//
//        //When
//        boolean result=productService.deleteProduct(id);
//
//
//        //Then
//        verify(productRepository).deleteById(id);
//        assertFalse(result);
//    }

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
        verify(productRepository).save(productCaptor.capture()); //verifies that the repository method was called
        Product savedProduct = productCaptor.getValue();
        assertEquals("Fan", savedProduct.getName());
        assertEquals("Description", savedProduct.getProductDescription());
        assertEquals("10", savedProduct.getQuantityInStock());
        assertEquals(10, savedProduct.getPrice());
    }

    @Test
    void getProductsDetailsTest(){
        //Given
        Product product1= new Product("123","Fan","Test product","1",120);
        Product product2= new Product("124","book","Test product","1",100);
        Product product3= new Product("125","Toy","Test product","1",50);
        List<String> productIds= List.of("123","124","125");
        List<Product> products = List.of(product1, product2, product3);
        when(productRepository.findAllById(productIds)).thenReturn(products);


        //When
        List<ProductDetails> result=productService.getProductDetails(productIds);


        //Then
        verify(productRepository).findAllById(productIds);
        assertEquals(3, result.size());

        assertTrue(result.stream().anyMatch(p -> p.name().equals("Fan") && p.price() == 120));
        assertTrue(result.stream().anyMatch(p -> p.name().equals("book") && p.price() == 100));
        assertTrue(result.stream().anyMatch(p -> p.name().equals("Toy") && p.price() == 50));



    }


    @Test
    void sendToOrderService() {
        // Given
        String action = "CREATED";
        String productId = "1234";

        // When
        productService.sendToOrderService(action, productId);

        // Then
        ArgumentCaptor<Message<ProductEvent>> productEventCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge).send(eq("productSupplier-out-0"), productEventCaptor.capture());
        Message<ProductEvent> sentMessage = productEventCaptor.getValue();
        ProductEvent event = sentMessage.getPayload();
        assertEquals("CREATED", event.getAction());
        assertEquals("1234", event.getPrimaryId());


    }

    @Test
    void getAllProducts() {
        //Given
        Product product1= new Product("123","product1","Test product","1",1);
        Product product2= new Product("121","product2","Test product","1",1);
        List<Product> products = List.of(product1, product2);
        productRepository.saveAll(products);
        Page<Product> page = new PageImpl<>(products);
        when(productRepository.findAll(PageRequest.of(0, 10))).thenReturn((page));// stub


        //When
        ProductDTO[] result = productService.getAllProducts(PageRequest.of(0, 10));

        //Then
        verify(productRepository).findAll(PageRequest.of(0, 10)); //mock

        assertEquals(2, result.length);
        assertEquals("product1", result[0].name());
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