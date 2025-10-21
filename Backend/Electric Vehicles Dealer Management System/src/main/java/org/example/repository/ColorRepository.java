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
}
