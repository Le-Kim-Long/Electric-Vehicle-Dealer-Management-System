package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.ConfigurationResponse;
import org.example.dto.UpdateConfigurationRequest;
import org.example.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configurations")
@Tag(name = "Configuration Management", description = "APIs for retrieving car variant configurations")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping("/variant/{variantId}")
    @Operation(
        summary = "Get configuration by variant ID",
        description = "Returns detailed configuration specifications for a specific car variant including battery, performance, and dimensions. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved configuration"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Variant or configuration not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getConfigurationByVariantId(@PathVariable Integer variantId) {
        try {
            ConfigurationResponse configuration = configurationService.getConfigurationByVariantId(variantId);
            return ResponseEntity.ok(configuration);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching configuration: " + e.getMessage());
        }
    }

    @GetMapping("/variant-name")
    @Operation(
        summary = "Get configuration by model name and variant name",
        description = "Returns detailed configuration specifications for a specific car variant by both model name and variant name including battery, performance, and dimensions. " +
                     "This ensures accurate results when variants have the same name across different models. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved configuration"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid model name or variant name"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Model-Variant combination or configuration not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getConfigurationByModelNameAndVariantName(
            @RequestParam String modelName,
            @RequestParam String variantName) {
        try {
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Model name is required and cannot be empty");
            }

            if (variantName == null || variantName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Variant name is required and cannot be empty");
            }

            ConfigurationResponse configuration = configurationService.getConfigurationByModelNameAndVariantName(
                modelName.trim(), variantName.trim());
            return ResponseEntity.ok(configuration);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching configuration: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(
        summary = "Update configuration by model name and variant name",
        description = "Updates configuration specifications for a specific car variant identified by model name and variant name. " +
                     "Only accessible by EVM Staff and Admin roles. All fields in the request body are optional - only provided fields will be updated. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('EVMStaff') or hasRole('Admin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated configuration"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid model name, variant name, or request body"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges (requires EVM Staff or Admin role)"),
        @ApiResponse(responseCode = "404", description = "Model-Variant combination or configuration not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateConfigurationByModelNameAndVariantName(
            @RequestParam String modelName,
            @RequestParam String variantName,
            @RequestBody UpdateConfigurationRequest request) {
        try {
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Model name is required and cannot be empty");
            }

            if (variantName == null || variantName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Variant name is required and cannot be empty");
            }

            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Request body is required");
            }

            ConfigurationResponse updatedConfiguration = configurationService.updateConfigurationByModelNameAndVariantName(
                modelName.trim(), variantName.trim(), request);
            return ResponseEntity.ok(updatedConfiguration);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating configuration: " + e.getMessage());
        }
    }
}
