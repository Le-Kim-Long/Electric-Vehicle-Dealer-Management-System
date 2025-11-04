package org.example.repository;

import org.example.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    @Query("SELECT p FROM Promotion p WHERE p.dealer.dealerId = :dealerId")
    List<Promotion> findByDealerId(@Param("dealerId") Integer dealerId);

    @Query("SELECT p FROM Promotion p WHERE p.dealer.dealerId = :dealerId AND p.status = :status")
    List<Promotion> findByDealerIdAndStatus(@Param("dealerId") Integer dealerId, @Param("status") String status);

    @Query("SELECT p FROM Promotion p WHERE p.promotionId = :promotionId AND p.dealer.dealerId = :dealerId")
    Promotion findByPromotionIdAndDealerId(@Param("promotionId") Integer promotionId, @Param("dealerId") Integer dealerId);

}
