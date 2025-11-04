package org.example.repository;

import org.example.entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer> {

    @Query("SELECT od FROM OrderDetails od " +
           "JOIN FETCH od.car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "WHERE od.order.orderId = :orderId")
    List<OrderDetails> findByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT COALESCE(SUM(" +
           "CASE WHEN od.finalPrice IS NOT NULL THEN od.finalPrice " +
           "ELSE (od.unitPrice * od.quantity) END), 0) " +
           "FROM OrderDetails od WHERE od.order.orderId = :orderId")
    BigDecimal sumOrderDetailsSubTotal(@Param("orderId") Integer orderId);
}
