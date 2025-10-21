package org.example.repository;

import org.example.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {

    Optional<Configuration> findByVariantId(Integer variantId);

    boolean existsByVariantId(Integer variantId);

    @Query("SELECT c FROM Configuration c JOIN c.carVariant cv WHERE cv.variantName = :variantName")
    Optional<Configuration> findByVariantName(@Param("variantName") String variantName);
}
