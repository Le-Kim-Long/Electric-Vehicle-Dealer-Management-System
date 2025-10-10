package org.example.repository;

import org.example.entity.Car;
import org.example.entity.CarVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarVariantRepository extends JpaRepository<CarVariant, Integer> {

    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> findAllVariantsWithConfiguration();

    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "JOIN cv.cars c " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> findVariantsByDealerIdWithConfiguration(@Param("dealerId") Integer dealerId);

    @Query("SELECT c FROM Car c WHERE c.variantId = :variantId ORDER BY c.colorId")
    List<Car> findCarsWithColorsByVariantId(@Param("variantId") Integer variantId);

    // Method cho admin/evmstaff - lấy tất cả màu trong hệ thống
    @Query("SELECT c.colorId, c.price, c.imagePath, SUM(dc.quantity) FROM Car c " +
           "JOIN c.dealerCars dc " +
           "WHERE c.variantId = :variantId " +
           "GROUP BY c.colorId, c.price, c.imagePath " +
           "ORDER BY c.colorId")
    List<Object[]> findColorIdsAndPricesByVariantId(@Param("variantId") Integer variantId);

    // Method cho dealerstaff - chỉ lấy màu của dealer hiện tại
    @Query("SELECT c.colorId, c.price, c.imagePath, dc.quantity FROM Car c " +
           "JOIN c.dealerCars dc " +
           "WHERE c.variantId = :variantId " +
           "AND dc.dealer.dealerId = :dealerId " +
           "GROUP BY c.colorId, c.price, c.imagePath, dc.quantity " +
           "ORDER BY c.colorId")
    List<Object[]> findColorIdsAndPricesByVariantIdAndDealerId(@Param("variantId") Integer variantId,
                                                               @Param("dealerId") Integer dealerId);

    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "JOIN cv.cars c " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId " +
           "AND (LOWER(cm.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(cv.variantName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(CONCAT(cm.modelName, ' ', cv.variantName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchVariantsByDealerIdAndTerm(@Param("dealerId") Integer dealerId,
                                                     @Param("searchTerm") String searchTerm);

    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "WHERE LOWER(cm.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(cv.variantName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(CONCAT(cm.modelName, ' ', cv.variantName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchAllVariantsByTerm(@Param("searchTerm") String searchTerm);
}
