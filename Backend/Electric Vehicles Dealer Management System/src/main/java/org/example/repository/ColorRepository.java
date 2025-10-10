package org.example.repository;

import org.example.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {

    @Query("SELECT c FROM Color c WHERE c.color_id IN :colorIds ORDER BY c.color_name")
    List<Color> findByColorIds(@Param("colorIds") List<Integer> colorIds);
}
