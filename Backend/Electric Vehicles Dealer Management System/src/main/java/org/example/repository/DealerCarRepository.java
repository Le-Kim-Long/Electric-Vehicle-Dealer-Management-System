package org.example.repository;

import org.example.entity.DealerCar;
import org.example.entity.DealerCarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    Optional<DealerCar> findByCarIdAndDealerId(Integer carId, Integer dealerId);

    // Method để tìm DealerCar theo dealerId và carId (thứ tự tham số khớp với ServiceImpl)
    Optional<DealerCar> findByDealerIdAndCarId(Integer dealerId, Integer carId);

    @Query("SELECT dc FROM DealerCar dc " +
           "JOIN FETCH dc.car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "WHERE dc.dealerId = :dealerId AND dc.status = 'On Sale'")
    List<DealerCar> findOnSaleDealerCarsByDealerId(@Param("dealerId") Integer dealerId);

    @Query("SELECT dc FROM DealerCar dc " +
           "JOIN FETCH dc.car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "JOIN dc.dealer d " +
           "WHERE d.dealerName = :dealerName AND dc.status = 'On Sale'")
    List<DealerCar> findOnSaleDealerCarsByDealerName(@Param("dealerName") String dealerName);

    @Query("DELETE FROM DealerCar dc WHERE dc.carId = :carId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByCarId(@Param("carId") Integer carId);

    @Query("SELECT dc FROM DealerCar dc " +
           "JOIN FETCH dc.car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color col " +
           "WHERE dc.dealerId = :dealerId " +
           "AND LOWER(cm.modelName) = LOWER(:modelName) " +
           "AND LOWER(cv.variantName) = LOWER(:variantName) " +
           "AND LOWER(col.colorName) = LOWER(:colorName)")
    Optional<DealerCar> findByModelVariantColorAndDealer(
        @Param("dealerId") Integer dealerId,
        @Param("modelName") String modelName,
        @Param("variantName") String variantName,
        @Param("colorName") String colorName
    );

}
