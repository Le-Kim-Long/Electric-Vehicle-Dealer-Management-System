package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.CarResponse;
import org.example.dto.CreateCompleteCarRequest;
import org.example.dto.AddCarToDealerRequest;
import org.example.dto.UpdateManufacturerPriceRequest;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/cars")
@Tag(name = "Car Management", description = "APIs for managing cars with role-based access control")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @PostMapping("/add-complete-car")
    @Operation(
        summary = "Add complete car with all details to system (Admin/EVMStaff only)",
        description = "Add a complete car with Model, Variant, Configuration, Color, and Car details. " +
                     "If Model/Variant/Configuration/Color already exists, it will be reused. " +
                     "If not, new records will be created. " +
                     "Only Admin and EVMStaff roles can access this endpoint. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Car successfully created with all details"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin/EVMStaff can add cars"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> addCompleteCarToSystem(@Valid @RequestBody CreateCompleteCarRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header is required. Please login first to get JWT token.");
            }

            String email = authentication.getName();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid authentication. Please login again.");
            }

            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVMStaff can add cars to the system.");
            }

            CarResponse carResponse = carService.addCompleteCarToSystem(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(carResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while adding complete car: " + e.getMessage());
        }
    }

    @PostMapping("/add-to-dealer")
    @Operation(
        summary = "Add car to dealer (Admin/EVMStaff only)",
        description = "Add a car to a specific dealer's inventory using model name, variant name, color name, dealer name and quantity. " +
                     "If the dealer already has this car (same model, variant, color), the quantity will be added to existing stock " +
                     "and existing dealer price and status will be kept unchanged. " +
                     "If the dealer doesn't have this car, a new entry will be created with dealer price = 0 and status = 'Pending'. " +
                     "Only Admin and EVMStaff roles can access this endpoint. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Car successfully added to dealer"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or car not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin/EVMStaff can add cars to dealers"),
        @ApiResponse(responseCode = "404", description = "Car or dealer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> addCarToDealer(@Valid @RequestBody AddCarToDealerRequest request) {
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
                    .body("Invalid authentication. Please login lại.");
            }

            // Lấy thông tin user để kiểm tra role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Chỉ Admin và EVMStaff mới có quyền thêm xe vào đại lý
            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVMStaff can add cars to dealers.");
            }

            // Thêm xe vào đại lý
            String result = carService.addCarToDealer(request);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while adding car to dealer: " + e.getMessage());
        }
    }

    @PutMapping("/update-manufacturer-price")
    @Operation(
        summary = "Update manufacturer price by model, variant, and color name (Admin/EVMStaff only)",
        description = "Updates the manufacturer price (giá niêm yết) for a specific car identified by model name, variant name, and color name. " +
                     "Only Admin and EVMStaff roles can access this endpoint. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manufacturer price successfully updated"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin/EVMStaff can update manufacturer prices"),
        @ApiResponse(responseCode = "404", description = "Car not found with specified model, variant, and color"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateManufacturerPrice(
            @RequestParam String modelName,
            @RequestParam String variantName,
            @RequestParam String colorName,
            @RequestBody @Valid UpdateManufacturerPriceRequest request) {
        try {
            // Check authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authorization header is required. Please login first to get JWT token.");
            }

            String email = authentication.getName();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid authentication. Please login again.");
            }

            // Check user role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String userRole = user.getRoleId().getRoleName();
            if (!"Admin".equals(userRole) && !"EVMStaff".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Admin and EVMStaff can update manufacturer prices.");
            }

            // Validate input parameters
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Model name is required and cannot be empty");
            }
            if (variantName == null || variantName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Variant name is required and cannot be empty");
            }
            if (colorName == null || colorName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Color name is required and cannot be empty");
            }

            String result = carService.updateManufacturerPrice(modelName, variantName, colorName, request);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating manufacturer price: " + e.getMessage());
        }
    }

    @GetMapping("/manufacturer-price")
    @Operation(
        summary = "Get manufacturer price by model, variant, and color name",
        description = "Retrieves the manufacturer price (giá niêm yết) for a specific car identified by model name, variant name, and color name. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved manufacturer price"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Car not found with specified model, variant, and color"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getManufacturerPrice(
            @RequestParam String modelName,
            @RequestParam String variantName,
            @RequestParam String colorName) {
        try {
            // Check authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authorization header is required. Please login first to get JWT token.");
            }

            String email = authentication.getName();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid authentication. Please login again.");
            }

            // Validate that user exists
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Validate input parameters
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Model name is required and cannot be empty");
            }
            if (variantName == null || variantName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Variant name is required and cannot be empty");
            }
            if (colorName == null || colorName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Color name is required and cannot be empty");
            }

            Long manufacturerPrice = carService.getManufacturerPrice(modelName, variantName, colorName);

            // Create response object
            String responseMessage = String.format("Car: %s %s (%s) - Manufacturer price: %s VND",
                    modelName.trim(), variantName.trim(), colorName.trim(),
                    manufacturerPrice != null ? String.format("%,d", manufacturerPrice) : "Not set");

            // Return both price and formatted message
            return ResponseEntity.ok(new ManufacturerPriceResponse(manufacturerPrice, responseMessage));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving manufacturer price: " + e.getMessage());
        }
    }

    // Response class for manufacturer price
    public static class ManufacturerPriceResponse {
        private Long manufacturerPrice;
        private String message;

        public ManufacturerPriceResponse(Long manufacturerPrice, String message) {
            this.manufacturerPrice = manufacturerPrice;
            this.message = message;
        }

        public Long getManufacturerPrice() {
            return manufacturerPrice;
        }

        public void setManufacturerPrice(Long manufacturerPrice) {
            this.manufacturerPrice = manufacturerPrice;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
