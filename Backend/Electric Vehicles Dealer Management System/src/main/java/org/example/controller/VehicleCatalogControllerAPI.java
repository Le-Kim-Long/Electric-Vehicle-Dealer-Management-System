package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.*;
import org.example.service.VehicleCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealer/catalog")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class VehicleCatalogControllerAPI {

    private final VehicleCatalogService catalogService;

    @GetMapping("/models")
    public ResponseEntity<List<CarModelResponse>> getAllModels() {
        return ResponseEntity.ok(catalogService.getAllModels());
    }

    @GetMapping("/models/{id}")
    public ResponseEntity<CarModelResponse> getModel(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogService.getModelById(id));
    }

    @GetMapping("/models/search")
    public ResponseEntity<List<CarModelResponse>> searchModels(@RequestParam String keyword) {
        return ResponseEntity.ok(catalogService.searchModels(keyword));
    }

    @GetMapping("/models/{modelId}/variants")
    public ResponseEntity<List<CarVariantResponse>> getVariantsByModel(@PathVariable Integer modelId) {
        return ResponseEntity.ok(catalogService.getVariantsByModel(modelId));
    }

    @GetMapping("/variants/{variantId}")
    public ResponseEntity<CarVariantResponse> getVariant(@PathVariable Integer variantId) {
        return ResponseEntity.ok(catalogService.getVariantById(variantId));
    }

    @GetMapping("/variants/{variantId}/cars")
    public ResponseEntity<List<CarResponse>> getCarsByVariant(@PathVariable Integer variantId) {
        return ResponseEntity.ok(catalogService.getCarsByVariant(variantId));
    }

    @GetMapping("/cars/{carId}")
    public ResponseEntity<CarResponse> getCar(@PathVariable Integer carId) {
        return ResponseEntity.ok(catalogService.getCarById(carId));
    }
}
