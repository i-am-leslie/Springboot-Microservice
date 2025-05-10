package com.example.productservice.service;


import com.example.productservice.DTO.ProductDTO;
import com.example.productservice.DTO.ProductRequestDTO;
import com.example.productservice.DTO.ProductEvent;
import com.example.productservice.model.Product;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import com.example.productservice.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cloud.stream.function.StreamBridge;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;


    private final StreamBridge streamBridge;


    private final  ProductFuzzySearch productFuzzySearch;

    private static final String BINDING_NAME ="productSupplier-out-0";


    public ProductService(ProductRepository productRepository, StreamBridge streamBridge, ProductFuzzySearch productFuzzySearch ){
        this.productRepository=productRepository;
        this.streamBridge=streamBridge;
        this.productFuzzySearch=productFuzzySearch;
    }


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
    @CircuitBreaker(name="product-service", fallbackMethod = "fallbackMethod")
    @Bulkhead(name="productServiceThreadPool",type = Bulkhead.Type.THREADPOOL,fallbackMethod = "fallbackMethod")
    public Product findProductByName(String productName){
        return productFuzzySearch.searchProducts(productName);
    }

    /**
     * Deletes the product from the database
     * @author Leslie
     * @param productName
     *
     */
    @CircuitBreaker(name="product service", fallbackMethod = "fallbackDeleteMethod")
    public boolean deleteProduct(String productName){
        String productId =  productRepository.findIdByName(productName);
        if(productId!=null){
            productRepository.deleteFirstByName(productName);
            sendToOrderService("DELETED", productId);
            logger.info("Product deleted");
            return true;
        }
        logger.info("Product Not found in database please check the name");
        return false;
    }

    /**
     * Creates a prodyct with the parameters
     *
     * @param productDTO
     */
    public  void createProduct(ProductRequestDTO productDTO){
        Product product = Product.builder()
                .productId(UUID.randomUUID().toString()) // Set productId
                .name(productDTO.name()) // Set productName
                .price(productDTO.price()) // Set price
                .productDescription(productDTO.description()) // set description
                .quantityInStock(productDTO.quantityInStock())
                .build(); // Build the product
        productRepository.save(product);
        sendToOrderService("CREATED", product.getProductId());
        log.info("created product {}",product.getName());
    }

    /**
     * Method to publish to kafka for order service to save to redis for caching. it is done asynchronously
     * @param action
     * @param productId
     *
     * @runtime  depends pn the streamBridge.send() method because it could fail and retry
     */
    @CircuitBreaker(name="product-service", fallbackMethod = "fallbackSendToOrderService")
    @Bulkhead(name="productServiceThreadPool", type = Bulkhead.Type.THREADPOOL)
    public void sendToOrderService(String action, String productId){
        ProductEvent productEvent;
        productEvent= ProductEvent.builder().action(action).primaryId(productId).build();
        streamBridge.setAsync(true);
        streamBridge.send(BINDING_NAME, MessageBuilder.withPayload(productEvent).build());
    }

    /**
     *  Gets all products in the database using pagination to split the data for efficiency and uses the stream api
     *  to convert all products to a productdto amd decided not to go wtih stream api because the data is low
     * @return products
     *
     * @runtime  0(log n) because we are working on a fixed subset in the iteration and log n because
     * pageable just returns a subset of the product data
     */
    public ProductDTO[] getAllProducts(Pageable page) {
        List<Product> listOfProducts= productRepository.findAll(page).getContent();
        ProductDTO[] products= new ProductDTO[listOfProducts.size()];
        for(int i=0; i < listOfProducts.size(); i++){
            Product product= listOfProducts.get(i);
            products[i]= new ProductDTO(product.getName(),
                    product.getProductDescription(),
                    product.getQuantityInStock(),
                    product.getPrice());
        }
//        List<ProductDTO> products= listOfProducts.stream().map((Product p)-> new ProductDTO(p.getName(),
//                p.getProductDescription(),
//                p.getQuantityInStock(),
//                p.getPrice())).collect(toList());
        return products;
    }


    /**
     * Gets the product with the specified id
     * @param id
     * @return id
     *
     * @runtime O(Log n)
     */
    public boolean getProductById(String id){
        return productRepository.existsById(id);
    }


    public Product fallbackMethod(String productName, Throwable throwable) {
        System.err.println("Fallback triggered for product: " + productName);
        System.err.println("Reason: " + throwable.getMessage());

        // You can return a default or null product
        return new Product();
    }

    public void fallbackDeleteMethod(String productName, Throwable throwable) {
        logger.error("Fallback triggered while deleting product: {}. Reason: {}", productName, throwable.getMessage());
    }

    public void fallbackSendToOrderService(String action,String productId, Throwable throwable) {
        logger.error("Fallback triggered while Sending to order service productId:{}. productName: {}. Reason: {}",productId, productId, throwable.getMessage());
    }

}
