package com.example.productservice.repository;

import com.example.productservice.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductRepository extends CrudRepository<Product,String> {
     List<Product> findByName(String productName);

     @Query("SELECT p.productId FROM Product p WHERE p.name = :productName")
     String findIdByName(String productName);

     @Modifying
     @Transactional
     @Query("delete From Product where name= :productName")
     void deleteFirstByName(String productName);

//     @Modifying
//     @Transactional
//     @Query("select p.name FROM  Product where name = :productName ORDER BY name ASC LIMIT 1 ")
//     Product findByname(String productName);

}
