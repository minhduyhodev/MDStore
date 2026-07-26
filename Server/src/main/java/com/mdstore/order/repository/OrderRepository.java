package com.mdstore.order.repository;

import com.mdstore.order.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderNo(String orderNo);

    @Query(value = """
            select * from orders
            where status = 'PROCESSING_RETRY' and next_retry_at <= :now
            order by next_retry_at, id
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<OrderEntity> findDueForRetry(@Param("now") Instant now,
                                      @Param("batchSize") int batchSize);

    @Query(value = """
            select * from orders
            where status = 'RETRYING' and retry_lease_until <= :now
            order by retry_lease_until, id
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<OrderEntity> findExpiredLeases(@Param("now") Instant now,
                                        @Param("batchSize") int batchSize);
}
