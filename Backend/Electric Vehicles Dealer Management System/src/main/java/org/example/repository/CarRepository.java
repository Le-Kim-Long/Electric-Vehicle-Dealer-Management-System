package org.example.repository;

import org.example.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {

    @Query("SELECT DISTINCT c FROM Car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId")
    List<Car> findCarsByDealerId(@Param("dealerId") Integer dealerId);

    @Query("SELECT DISTINCT c FROM Car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "JOIN c.dealerCars dc " +
           "JOIN dc.dealer d " +
           "WHERE d.dealerName = :dealerName")
    List<Car> findCarsByDealerName(@Param("dealerName") String dealerName);

    @Query("SELECT DISTINCT c FROM Car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId " +
           "AND (LOWER(cv.variantName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(cm.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Car> searchCarsByVariantOrModelName(@Param("dealerId") Integer dealerId, @Param("searchTerm" ) String searchTerm);

    @Query("SELECT DISTINCT c FROM Car c " +
           "JOIN FETCH c.carVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH c.color " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId " +
           "AND c.price BETWEEN :minPrice AND :maxPrice")
    List<Car> findCarsByDealerIdAndPriceRange(@Param("dealerId") Integer dealerId,
                                              @Param("minPrice") Double minPrice,
                                              @Param("maxPrice") Double maxPrice);

    @Query("SELECT DISTINCT c FROM Car c " +
           "LEFT JOIN FETCH c.color " +
           "JOIN c.dealerCars dc " +
           "WHERE c.variantId = :variantId " +
           "AND dc.dealer.dealerId = :dealerId")
    List<Car> findCarsByVariantIdAndDealerId(@Param("variantId") Integer variantId, @Param("dealerId") Integer dealerId);

    @Query("SELECT c FROM Car c " +
           "WHERE c.variantId = :variantId " +
           "AND c.colorId = :colorId")
    List<Car> findDuplicateCars(@Param("variantId") Integer variantId,
                               @Param("colorId") Integer colorId);

    @Query("SELECT COUNT(c) > 0 FROM Car c " +
           "WHERE c.variantId = :variantId " +
           "AND c.colorId = :colorId")
    boolean existsDuplicateCar(@Param("variantId") Integer variantId,
                              @Param("colorId") Integer colorId);
}
