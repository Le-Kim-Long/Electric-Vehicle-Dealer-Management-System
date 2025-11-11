package org.example.repository;

import org.example.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    /**
     * Lấy tất cả orders của một dealer cụ thể
     */
    @Query("SELECT o FROM Orders o " +
           "WHERE o.dealer.dealerId = :dealerId " +
           "ORDER BY o.orderDate DESC")
    List<Orders> findAllByDealerId(@Param("dealerId") Integer dealerId);

    /**
     * Lấy order details của một order cụ thể
     */
    @Query("SELECT DISTINCT o FROM Orders o " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.car c " +
           "LEFT JOIN FETCH c.carVariant cv " +
           "LEFT JOIN FETCH cv.carModel " +
           "WHERE o.orderId = :orderId")
    Orders findOrderWithDetails(@Param("orderId") Integer orderId);
}
