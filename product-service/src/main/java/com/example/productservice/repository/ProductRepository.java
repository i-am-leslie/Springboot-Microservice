package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductRepository extends CrudRepository<Product,String> {
     List<Product> findByName(String productName);

}
