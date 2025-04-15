package com.example.productservice.service;


import com.example.productservice.DTO.ProductEvent;
import com.example.productservice.model.Product;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import com.example.productservice.repository.ProductRepository;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cloud.stream.function.StreamBridge;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private ProductSearchHelper productSearchHelper;

    private static final String BINDING_NAME ="stringSupplier-out-0";


    /**
     * Not in use yet
     * @param product
     */

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

    /**
     * Gets the product that is being searched for
     *
     * @author Leslie
     * @param productName The string name
     * @return Product  The prodyct that needs to be found
     */
    @CircuitBreaker(name="product service", fallbackMethod = "fallbackMethod")
    @Bulkhead(name="product service",type = Bulkhead.Type.THREADPOOL,fallbackMethod = "fallbackMethod")
    public Product findProductByName(String productName){
//        Iterable<Product> products=productRepository.findAll(); // memory inefficient for large data sets
//        for(Product p :products ){
//            if (p.getName().equals(productName)){
//                System.out.println(p);
//                return p;
//            }
//        }
//        throw new RuntimeException("Product not found");
        return productSearchHelper.searchProducts(productName);
    }

    /**
     * Deletes the product from the database
     *
     * @param productName
     *
     */
    public void deleteProduct(String productName){
        String productId =  productRepository.findIdByName(productName);
        if(productId!=null){
            productRepository.deleteFirstByName(productName);
            sendToRedisCache("DELETED", productId);
            logger.info("Product deleted");
        }else{
            logger.info("Product Not found in database please check the name");
        }

    }

    /**
     * Creates a prodyct with the parameters
     *
     * @param productName
     * @param price
     * @param productDescritption
     */
    public  void createProduct(String productName, Integer price,String productDescritption){
        Product product = Product.builder()
                .productId(UUID.randomUUID().toString()) // Set productId
                .name(productName) // Set productName
                .price(price) // Set price
                .productDescription(productDescritption)
                .build(); // Build the product
        productRepository.save(product);
        sendToRedisCache("CREATED", product.getProductId());
        log.info("created product {}",productName);

    }
    public void sendToRedisCache(String action,String productId ){
        ProductEvent productEvent;
        productEvent= ProductEvent.builder().action(action).primaryId(productId).build();
        streamBridge.setAsync(true);
        streamBridge.send(BINDING_NAME, MessageBuilder.withPayload(productEvent).build());

    }

    /**
     *  Gets all products in the database
     * @return products
     */
    public Iterable<Product> getAllProducts() {
        return productRepository.findAll();
    }


    /**
     * Gets the product with the specified id
     * @param id
     * @return id
     *
     * @runtime O(Log n)
     */
    public String getProductById(String id){
        if(productRepository.findById(id).isPresent()){
            Product product=  productRepository.findById(id).get();
            logger.info("Product found");
            return product.toString();
        }
        return "null";
    }


    public Product fallbackMethod(String productName, Throwable throwable) {
        System.err.println("Fallback triggered for product: " + productName);
        System.err.println("Reason: " + throwable.getMessage());

        // You can return a default or null product
        return new Product();
    }

}
