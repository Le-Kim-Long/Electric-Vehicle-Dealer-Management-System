package org.example.repository;

import org.example.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {

    @Query("SELECT c FROM Color c WHERE c.colorId IN :colorIds ORDER BY c.colorName")
    List<Color> findByColorIds(@Param("colorIds") List<Integer> colorIds);

    Optional<Color> findByColorNameIgnoreCase(String colorName);

    boolean existsByColorNameIgnoreCase(String colorName);

    @Query("SELECT DISTINCT c.colorName FROM Color c ORDER BY c.colorName")
    List<String> findAllColorNames();

    @Query("SELECT DISTINCT c.colorName FROM Color c " +
           "JOIN Car car ON c.colorId = car.colorId " +
           "JOIN CarVariant cv ON car.variantId = cv.variantId " +
           "JOIN CarModel cm ON cv.modelId = cm.modelId " +
           "WHERE LOWER(cm.modelName) = LOWER(:modelName) " +
           "AND LOWER(cv.variantName) = LOWER(:variantName) " +
           "ORDER BY c.colorName")
    List<String> findColorNamesByModelNameAndVariantName(@Param("modelName") String modelName,
                                                        @Param("variantName") String variantName);
}
