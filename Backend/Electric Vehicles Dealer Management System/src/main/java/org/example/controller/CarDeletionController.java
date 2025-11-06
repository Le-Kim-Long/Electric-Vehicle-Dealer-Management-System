package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.service.CarDeletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cars")
@Tag(name = "Car Deletion Management", description = "APIs for deleting cars, variants, and models - Admin and EVM Staff only")
public class CarDeletionController {

    @Autowired
    private CarDeletionService carDeletionService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @DeleteMapping("/delete")
    @Operation(
        summary = "Delete cars, variants, or models",
        description = "Delete cars based on provided parameters with cascading deletion logic. " +
                     "Only accessible by Admin and EVM Staff roles. " +
                     "If modelName, variantName, and colorName are provided: deletes cars with those specifications. " +
                     "If modelName and variantName are provided: deletes the entire variant and all its cars. " +
                     "If only modelName is provided: deletes the entire model and all its variants and cars. " +
                     "Auto-cascading: If deleting cars results in empty variant, the variant is also deleted. " +
                     "If deleting variant results in empty model, the model is also deleted. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted cars/variants/models"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or nothing found to delete"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin and EVM Staff can delete cars"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteCars(
            @Parameter(description = "Model name (required). Example: VF8, VF9",
                      example = "VF8", required = true)
            @RequestParam String modelName,
            @Parameter(description = "Variant name (optional). If provided, deletes specific variant. Example: Eco, Plus",
                      example = "Eco", required = false)
            @RequestParam(required = false) String variantName,
            @Parameter(description = "Color name (optional). If provided with variantName, deletes specific cars. Example: Trắng, Đen",
                      example = "Trắng", required = false)
            @RequestParam(required = false) String colorName) {
        try {
            // Lấy authentication từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header is required. Please login first to get JWT token.");
            }

            // Lấy email từ authentication (JWT subject)
            String email = authentication.getName();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid authentication. Please login again.");
            }

            // Lấy thông tin user để kiểm tra role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Chỉ cho phép Admin và EVM Staff xóa xe
            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVM Staff can delete cars, variants, and models.");
            }

            // Validate input
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Model name is required.");
            }

            // Xóa xe với logic cascading
            String result = carDeletionService.deleteCars(modelName.trim(),
                                                         variantName != null ? variantName.trim() : null,
                                                         colorName != null ? colorName.trim() : null);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            // Xử lý các lỗi cụ thể
            String errorMessage = e.getMessage();

            if (errorMessage.contains("not found") || errorMessage.contains("No cars found")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while deleting cars: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-by-model")
    @Operation(
        summary = "Delete entire model and all its data",
        description = "Delete an entire car model including all its variants, configurations, and cars. " +
                     "Only accessible by Admin and EVM Staff roles. " +
                     "This is a convenience endpoint that deletes everything related to a model. " +
                     "Use with caution as this action is irreversible. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted model and all related data"),
        @ApiResponse(responseCode = "400", description = "Bad request - Model not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin and EVM Staff can delete models"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteModel(
            @Parameter(description = "Model name to delete completely. Example: VF8, VF9",
                      example = "VF8", required = true)
            @RequestParam String modelName) {
        try {
            // Lấy authentication từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header is required. Please login first to get JWT token.");
            }

            // Lấy email từ authentication (JWT subject)
            String email = authentication.getName();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid authentication. Please login again.");
            }

            // Lấy thông tin user để kiểm tra role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Chỉ cho phép Admin và EVM Staff xóa model
            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVM Staff can delete car models.");
            }

            // Validate input
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Model name is required.");
            }

            // Xóa toàn bộ model
            String result = carDeletionService.deleteCars(modelName.trim(), null, null);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            // Xử lý các lỗi cụ thể
            String errorMessage = e.getMessage();

            if (errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while deleting model: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-variant")
    @Operation(
        summary = "Delete specific variant and all its cars",
        description = "Delete a specific car variant including all its cars and configuration. " +
                     "Only accessible by Admin and EVM Staff roles. " +
                     "If this is the last variant of the model, the model will also be deleted. " +
                     "Use with caution as this action is irreversible. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted variant and all related data"),
        @ApiResponse(responseCode = "400", description = "Bad request - Model or variant not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin and EVM Staff can delete variants"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteVariant(
            @Parameter(description = "Model name containing the variant. Example: VF8, VF9",
                      example = "VF8", required = true)
            @RequestParam String modelName,
            @Parameter(description = "Variant name to delete. Example: Eco, Plus",
                      example = "Eco", required = true)
            @RequestParam String variantName) {
        try {
            // Lấy authentication từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header is required. Please login first to get JWT token.");
            }

            // Lấy email từ authentication (JWT subject)
            String email = authentication.getName();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid authentication. Please login again.");
            }

            // Lấy thông tin user để kiểm tra role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Chỉ cho phép Admin và EVM Staff xóa variant
            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVM Staff can delete car variants.");
            }

            // Validate input
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Model name is required.");
            }
            if (variantName == null || variantName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Variant name is required.");
            }

            // Xóa variant
            String result = carDeletionService.deleteCars(modelName.trim(), variantName.trim(), null);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            // Xử lý các lỗi cụ thể
            String errorMessage = e.getMessage();

            if (errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while deleting variant: " + e.getMessage());
        }
    }
}
