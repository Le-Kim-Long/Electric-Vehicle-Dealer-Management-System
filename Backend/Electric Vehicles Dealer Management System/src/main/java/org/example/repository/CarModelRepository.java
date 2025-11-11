package org.example.repository;

import org.example.entity.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarModelRepository extends JpaRepository<CarModel, Integer> {

    Optional<CarModel> findByModelNameIgnoreCase(String modelName);

    boolean existsByModelNameIgnoreCase(String modelName);

    @Query("SELECT DISTINCT c.modelName FROM CarModel c ORDER BY c.modelName")
    List<String> findAllModelNames();

    @Query("SELECT c.segment FROM CarModel c WHERE c.modelName = :modelName")
    Optional<String> findSegmentByModelName(@Param("modelName") String modelName);
}
