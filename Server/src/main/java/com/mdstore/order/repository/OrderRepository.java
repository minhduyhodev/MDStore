package com.mdstore.order.repository;

import com.mdstore.order.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    
    Optional<OrderEntity> findByOrderNo(String orderNo);
}
