package com.example.productservice.service;

import com.example.productservice.model.Product;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.productservice.repository.ProductRepository;

import java.util.UUID;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    @Autowired
    private ProductRepository productRepository;



    public  void saveProduct(Product product){
        logger.info("Saving product: {}", product.getName());
        if (product.getProductId()==null){
            product.setProductId(UUID.randomUUID().toString());
        }else{
            return;
        }
        logger.info("Saved product: {}", product.getName());
        productRepository.save(product);
    }
    @CircuitBreaker(name="product service", fallbackMethod = "fallbackMethod")
    @Bulkhead(name="product service",type = Bulkhead.Type.THREADPOOL,fallbackMethod = "fallbackMethod")
    public Product findProductByName(String productName){
        Iterable<Product> products=productRepository.findAll();
        for(Product p :products ){
            if (p.getName().equals(productName)){
                System.out.println(p);
                return p;
            }
        }
        throw new RuntimeException("Product not found");
    }

    public void deleteProduct(String productName){
        Iterable<Product> products=productRepository.findAll();
        for(Product p :products ){
            if (p.getName().equals(productName)){
                System.out.println(p);
                ;productRepository.deleteById(p.getProductId());
            }
        }
    }

    public  void createProduct(String productName, Integer price,String productDescritption){
        Product product = Product.builder()
                .productId(UUID.randomUUID().toString()) // Set productId
                .name(productName) // Set productName
                .price(price) // Set price
                .productDescription(productDescritption)
                .build(); // Build the product
        productRepository.save(product);
    }

    public Iterable<Product> getAllProducts() {
        Iterable<Product> products= productRepository.findAll();
        return products;
    }

    public String getProductById(String id){
        Iterable<Product> products=productRepository.findAll();
        for(Product p :products ){
            if (p.getProductId().equals(id)){
                System.out.println(p);
                return id;
            }
        }

        return  null;
    }

    public Product fallbackMethod(String productName, Throwable throwable) {
        System.err.println("Fallback triggered for product: " + productName);
        System.err.println("Reason: " + throwable.getMessage());

        // You can return a default or null product
        return new Product();
    }

}
