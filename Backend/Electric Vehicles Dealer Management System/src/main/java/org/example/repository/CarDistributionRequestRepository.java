package org.example.repository;

import org.example.entity.CarDistributionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarDistributionRequestRepository extends JpaRepository<CarDistributionRequest, Integer> {

    // Find requests by dealer ID
    @Query("SELECT cdr FROM CarDistributionRequest cdr WHERE cdr.dealer.dealerId = :dealerId ORDER BY cdr.requestDate DESC")
    List<CarDistributionRequest> findByDealerId(@Param("dealerId") Integer dealerId);

    // Find requests by status
    @Query("SELECT cdr FROM CarDistributionRequest cdr WHERE cdr.status = :status ORDER BY cdr.requestDate DESC")
    List<CarDistributionRequest> findByStatus(@Param("status") String status);

    // Find requests by dealer ID and status
    @Query("SELECT cdr FROM CarDistributionRequest cdr WHERE cdr.dealer.dealerId = :dealerId AND cdr.status = :status ORDER BY cdr.requestDate DESC")
    List<CarDistributionRequest> findByDealerIdAndStatus(@Param("dealerId") Integer dealerId, @Param("status") String status);
}
