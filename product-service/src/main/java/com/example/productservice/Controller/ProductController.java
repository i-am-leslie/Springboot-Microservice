package com.example.productservice.Controller;

import com.example.productservice.DTO.ProductDTO;
import com.example.productservice.DTO.ProductDetails;
import com.example.productservice.DTO.ProductRequestDTO;
import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping(value="api/v1/products")
@Slf4j
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping(value = "/get/{productName}")
    public ResponseEntity<Product> getProduct(@PathVariable("productName") String productName){
        return ResponseEntity.ok(productService.findProductByName(productName));
    }

    @GetMapping(value = "/getId/{id}")
    public ResponseEntity<String>  getProductById(@PathVariable("id") String id){  // need refractoring for the order service
        boolean productExist=productService.getProductById(id);
        if(productExist) return ResponseEntity.ok(id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/getAll")
    public ResponseEntity<ProductDTO[]>  allProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                     @RequestParam(name = "size", defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }


    @PostMapping(value = "/create")
    public ResponseEntity<String> create(@Valid @RequestBody ProductRequestDTO product){
        log.info("Saving product {}", product.name());
        productService.createProduct(product);
        return ResponseEntity.ok("Product created");
    }

    @PostMapping(value= "/getProductDetails")
    public ResponseEntity<List<ProductDetails>> getProductDetails(@RequestBody List<String> ids){
        return ResponseEntity.ok(productService.getProductDetails(ids));
    }


    @DeleteMapping(value = "/delete/{productName}")  // refractoring  to use id because name is not reliable
    public ResponseEntity<String> deleteProduct(@PathVariable("productName")String productId){
        if(productService.deleteProduct(productId)) return  ResponseEntity.ok("Product deleted successfully");
        return ResponseEntity.notFound().build();
    }


    //Create api that filters based on user

}
