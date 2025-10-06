package org.example.repository;

import org.example.entity.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarModelRepository  extends JpaRepository<CarModel, Integer> {
    List<CarModel> findByModelNameContainingIgnoreCase(String keyword);
}