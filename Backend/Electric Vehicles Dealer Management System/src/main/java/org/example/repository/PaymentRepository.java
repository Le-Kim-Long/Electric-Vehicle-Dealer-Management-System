package org.example.repository;

import org.example.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId ORDER BY p.paymentDate DESC")
    List<Payment> findByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId AND p.status = :status")
    List<Payment> findByOrderIdAndStatus(@Param("orderId") Integer orderId, @Param("status") String status);

    @Query("SELECT p FROM Payment p JOIN p.order o JOIN o.dealer d JOIN UserAccount u ON u.dealer = d WHERE u.email = :email ORDER BY p.paymentDate DESC")
    List<Payment> findAllByDealerUserEmail(@Param("email") String email);
}
