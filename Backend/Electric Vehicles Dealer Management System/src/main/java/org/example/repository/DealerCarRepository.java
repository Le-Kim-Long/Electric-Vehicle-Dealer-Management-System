package org.example.repository;

import org.example.entity.DealerCar;
import org.example.entity.DealerCarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealerCarRepository extends JpaRepository<DealerCar, DealerCarId> {

    @Query("SELECT dc FROM DealerCar dc " +
           "JOIN FETCH dc.car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "WHERE dc.dealerId = :dealerId")
    List<DealerCar> findDealerCarsByDealerId(@Param("dealerId") Integer dealerId);

    @Query("SELECT dc FROM DealerCar dc " +
           "JOIN FETCH dc.car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "JOIN dc.dealer d " +
           "WHERE d.dealerName = :dealerName")
    List<DealerCar> findDealerCarsByDealerName(@Param("dealerName") String dealerName);
}
