package com.example.productservice.repository;

import com.example.productservice.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,String> {

     @Query("SELECT p.productId FROM Product p WHERE p.name = :productName")
     String findIdByName(String productName);

     @Modifying
     @Transactional
     @Query("DELETE FROM Product  WHERE productId= :productId")
     int deleteProductById(String productId);


}
