package org.example.repository;

import org.example.entity.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Integer> {

    @Query("SELECT i FROM Installment i WHERE i.order.orderId = :orderId")
    Optional<Installment> findByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT i FROM Installment i JOIN i.order o JOIN o.dealer d JOIN UserAccount u ON u.dealer = d WHERE u.email = :email")
    List<Installment> findAllByDealerUserEmail(@Param("email") String email);

    @Query("SELECT i FROM Installment i WHERE i.order.orderId = :orderId AND EXISTS " +
           "(SELECT 1 FROM UserAccount u WHERE u.email = :email AND u.dealer = i.order.dealer)")
    Optional<Installment> findByOrderIdAndDealerUserEmail(@Param("orderId") Integer orderId, @Param("email") String email);
}
