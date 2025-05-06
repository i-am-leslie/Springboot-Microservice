package com.example.productservice.Controller;

import com.example.productservice.DTO.ProductRequestDTO;
import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
@RestController
@RequestMapping(value="api/v1/products")
@Slf4j
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping(value = "/get/{productName}")
    public ResponseEntity<?> getProduct(@PathVariable("productName") String productName){
        return ResponseEntity.ok(productService.findProductByName(productName));
    }

    @GetMapping(value = "/getId/{id}")
    public ResponseEntity<?>  getProductById(@PathVariable("id") String id){  // need refractoring for the order service
        boolean productExist=productService.getProductById(id);
        if(productExist){
            return ResponseEntity.ok(id);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/getAll")
    public ResponseEntity<?>  allProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }


    @PostMapping(value = "/create")
    public ResponseEntity<?> create(@Valid @RequestBody ProductRequestDTO product){
        log.info("Saving product {}", product.name());
        productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(value = "/delete/{productName}")  // refractoring  to use id because name is not reliable
    public ResponseEntity<?> deleteProduct(@PathVariable("productName")String productName){
        if(productService.deleteProduct(productName)) return  ResponseEntity.status(HttpStatus.OK).build();
        return ResponseEntity.notFound().build();
    }


}
