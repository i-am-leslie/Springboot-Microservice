package com.example.orderservice.repository;

import com.example.orderservice.model.Orders;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface OrderRepository extends CrudRepository<Orders, String> {
     Optional<Orders> findById(String orderId);

}
