package org.example.repository;

import org.example.entity.CarVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CarVariantRepository extends JpaRepository<CarVariant, Integer> {
    List<CarVariant> findByModelId(Integer modelId);
}
