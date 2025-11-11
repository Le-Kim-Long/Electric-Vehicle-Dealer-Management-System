package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.service.CarModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/car-models")
@RequiredArgsConstructor
@Tag(name = "Car Model", description = "Car Model Management APIs")
public class CarModelController {

    private final CarModelService carModelService;

    @GetMapping("/model-names")
    @Operation(summary = "Get all model names", description = "Retrieve all available model names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved model names"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    public ResponseEntity<List<String>> getAllModelNames() {
        List<String> modelNames = carModelService.getAllModelNames();
        return ResponseEntity.ok(modelNames);
    }

    @GetMapping("/segment")
    @Operation(summary = "Get segment by model name", description = "Retrieve segment information for a specific model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved segment"),
            @ApiResponse(responseCode = "404", description = "Model not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })

    public ResponseEntity<String> getSegmentByModelName(
            @Parameter(description = "Model name to get segment for", required = true)
            @RequestParam String modelName) {
        String segment = carModelService.getSegmentByModelName(modelName);
        if (segment != null) {
            return ResponseEntity.ok(segment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
