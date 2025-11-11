package org.example.repository;

import org.example.entity.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Integer> {

    @Query("SELECT d.dealerName FROM Dealer d ORDER BY d.dealerName ASC")
    List<String> findAllDealerNames();

    @Query("SELECT d FROM Dealer d ORDER BY d.dealerName ASC")
    List<Dealer> findAllDealersOrderByName();

    Optional<Dealer> findByDealerName(String dealerName);
}
