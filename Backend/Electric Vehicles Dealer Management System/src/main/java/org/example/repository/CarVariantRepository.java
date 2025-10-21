package org.example.repository;

import org.example.entity.Car;
import org.example.entity.CarVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // Method cho admin/evmstaff - lấy tất cả màu trong hệ thống không cần quantity
    @Query("SELECT DISTINCT c.colorId, c.price, c.imagePath FROM Car c " +
           "WHERE c.variantId = :variantId " +
           "ORDER BY c.colorId")
    List<Object[]> findColorIdsAndPricesWithoutQuantityByVariantId(@Param("variantId") Integer variantId);

    // Method cho dealerstaff - chỉ lấy màu của dealer hiện tại
    @Query("SELECT c.colorId, c.price, c.imagePath, dc.quantity FROM Car c " +
           "JOIN c.dealerCars dc " +
           "WHERE c.variantId = :variantId " +
           "AND dc.dealer.dealerId = :dealerId " +
           "GROUP BY c.colorId, c.price, c.imagePath, dc.quantity " +
           "ORDER BY c.colorId")
    List<Object[]> findColorIdsAndPricesByVariantIdAndDealerId(@Param("variantId") Integer variantId,
                                                               @Param("dealerId") Integer dealerId);

    // Method cho API getVariantDetailsByDealerName - lấy variants theo dealer name
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "JOIN cv.cars c " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerName = :dealerName " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> findVariantsByDealerNameWithConfiguration(@Param("dealerName") String dealerName);

    // Method cho API getVariantDetailsByDealerName - lấy color info theo dealer name
    @Query("SELECT c.colorId, c.price, c.imagePath, dc.quantity FROM Car c " +
           "JOIN c.dealerCars dc " +
           "WHERE c.variantId = :variantId " +
           "AND dc.dealer.dealerName = :dealerName " +
           "GROUP BY c.colorId, c.price, c.imagePath, dc.quantity " +
           "ORDER BY c.colorId")
    List<Object[]> findColorIdsAndPricesByVariantIdAndDealerName(@Param("variantId") Integer variantId,
                                                                 @Param("dealerName") String dealerName);

    // Search methods cho admin/evmstaff
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "WHERE LOWER(CONCAT(cm.modelName, ' ', cv.variantName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchAllVariantsWithConfiguration(@Param("searchTerm") String searchTerm);

    // Search methods cho dealerstaff
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "JOIN cv.cars c " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId " +
           "AND LOWER(CONCAT(cm.modelName, ' ', cv.variantName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchVariantsByDealerIdAndTerm(@Param("dealerId") Integer dealerId,
                                                     @Param("searchTerm") String searchTerm);

    // Search methods by variant name specifically - cho admin/evmstaff
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "WHERE LOWER(cv.variantName) LIKE LOWER(CONCAT('%', :variantName, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchVariantsByVariantNameInSystem(@Param("variantName") String variantName);

    // Search methods by variant name specifically - cho dealerstaff
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "JOIN cv.cars c " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId " +
           "AND LOWER(cv.variantName) LIKE LOWER(CONCAT('%', :variantName, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchVariantsByVariantNameAndDealerId(@Param("dealerId") Integer dealerId,
                                                            @Param("variantName") String variantName);

    // Search methods by model name specifically - cho admin/evmstaff
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "WHERE LOWER(cm.modelName) LIKE LOWER(CONCAT('%', :modelName, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchVariantsByModelNameInSystem(@Param("modelName") String modelName);

    // Search methods by model name specifically - cho dealerstaff
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "JOIN cv.cars c " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId " +
           "AND LOWER(cm.modelName) LIKE LOWER(CONCAT('%', :modelName, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchVariantsByModelNameAndDealerId(@Param("dealerId") Integer dealerId,
                                                          @Param("modelName") String modelName);

    // Search methods by both model name and variant name - cho admin/evmstaff
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "WHERE LOWER(cm.modelName) LIKE LOWER(CONCAT('%', :modelName, '%')) " +
           "AND LOWER(cv.variantName) LIKE LOWER(CONCAT('%', :variantName, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchVariantsByModelAndVariantNameInSystem(@Param("modelName") String modelName,
                                                                 @Param("variantName") String variantName);

    // Search methods by both model name and variant name - cho dealerstaff
    @Query("SELECT DISTINCT cv FROM CarVariant cv " +
           "JOIN FETCH cv.carModel cm " +
           "LEFT JOIN FETCH cv.configuration " +
           "JOIN cv.cars c " +
           "JOIN c.dealerCars dc " +
           "WHERE dc.dealer.dealerId = :dealerId " +
           "AND LOWER(cm.modelName) LIKE LOWER(CONCAT('%', :modelName, '%')) " +
           "AND LOWER(cv.variantName) LIKE LOWER(CONCAT('%', :variantName, '%')) " +
           "ORDER BY cm.modelName, cv.variantName")
    List<CarVariant> searchVariantsByModelAndVariantNameAndDealerId(@Param("dealerId") Integer dealerId,
                                                                    @Param("modelName") String modelName,
                                                                    @Param("variantName") String variantName);

    Optional<CarVariant> findByVariantNameIgnoreCaseAndModelId(String variantName, Integer modelId);

    boolean existsByVariantNameIgnoreCaseAndModelId(String variantName, Integer modelId);

    @Query("SELECT DISTINCT cv.variantName FROM CarVariant cv ORDER BY cv.variantName")
    List<String> findAllVariantNames();

    @Query("SELECT cv.description FROM CarVariant cv WHERE cv.variantName = :variantName")
    Optional<String> findDescriptionByVariantName(@Param("variantName") String variantName);
}
