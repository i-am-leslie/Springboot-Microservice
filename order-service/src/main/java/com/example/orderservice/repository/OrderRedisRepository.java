package com.example.orderservice.repository;

import com.example.orderservice.model.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRedisRepository extends CrudRepository<Product,String> {
}
