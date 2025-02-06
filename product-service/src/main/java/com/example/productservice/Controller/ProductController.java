package com.example.productservice.Controller;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value="/v1/product")
@Slf4j
public class ProductController {
    @Autowired
    private ProductService productService;






    @GetMapping(value = "/getProduct/{productName}")
    public Product getProduct(@PathVariable("productName") String productName){
        return productService.findProductByName(productName);
    }

    @GetMapping(value = "/getProductById/{id}")
    public String getProductById(@PathVariable("id") String id){
        if(productService.getProductById(id).equals("null")){
            return "null";
        }else{
            return id;
        }
    }

    @GetMapping(value = "/getAllProducts")
    public Iterable<Product> allProducts(){
        return productService.getAllProducts();
    }


    @PostMapping(value = "/create")
    public void  saveProduct(@RequestBody Product product){
        log.info("Saving product {}", product.getName());
        productService.createProduct(product.getName(), product.getPrice(),product.getProductDescription());
    }

    @DeleteMapping(value = "/delete/{productName}")
    public void deleteProduct(@PathVariable("productName")String productName){
        productService.deleteProduct(productName);
    }


}
