package com.mdstore.order.repository;

import com.mdstore.order.domain.DeliveredAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveredAccountRepository extends JpaRepository<DeliveredAccountEntity, Long> {
}
