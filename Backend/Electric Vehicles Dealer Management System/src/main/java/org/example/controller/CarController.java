package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.CarResponse;
import org.example.dto.CreateCarRequest;
import org.example.dto.CreateCompleteCarRequest;
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

    @GetMapping("/all-cars")
    @Operation(
        summary = "Get all cars based on user role",
        description = "Returns cars based on user's role: " +
                     "Admin/EVMStaff can see all cars in the system, " +
                     "DealerStaff can only see cars of their dealer. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cars"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllCars() {
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

            String roleName = user.getRole_id().getRole_name();

            List<CarResponse> cars;

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff xem tất cả xe trong hệ thống
                cars = carService.getAllCarsInSystem();
            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff chỉ xem xe của dealer mình
                cars = carService.getAllCarsByCurrentDealer(email);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to view cars.");
            }

            return ResponseEntity.ok(cars);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching cars: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search cars by variant name or model name (role-based)",
        description = "Search cars based on user's role: " +
                     "Admin/EVMStaff can search all cars in the system, " +
                     "DealerStaff can only search cars of their dealer. " +
                     "Supports flexible search with multiple keywords (e.g., 'vf3 eco', 'vinfast plus'). " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid search term"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchCars(@RequestParam(value = "searchTerm", required = true) String searchTerm) {
        try {
            // Kiểm tra search term không rỗng
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Search term cannot be empty");
            }

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

            String roleName = user.getRole_id().getRole_name();

            List<CarResponse> cars;

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff tìm kiếm trong tất cả xe của hệ thống
                cars = carService.searchCarsInSystem(searchTerm.trim());
            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff chỉ tìm kiếm trong xe của dealer mình
                cars = carService.searchCarsByVariantOrModelName(email, searchTerm.trim());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to search cars.");
            }

            return ResponseEntity.ok(cars);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching cars: " + e.getMessage());
        }
    }

    @PostMapping("/add-car")
    @Operation(
        summary = "Add new car to system (Admin/EVMStaff only)",
        description = "Add a new car to the system. This car will not be assigned to any dealer initially. " +
                     "Only Admin and EVMStaff roles can access this endpoint. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Car successfully created"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin/EVMStaff can add cars"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> addCarToSystem(@Valid @RequestBody CreateCarRequest request) {
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

            String roleName = user.getRole_id().getRole_name();

            // Chỉ Admin và EVMStaff mới có quyền thêm xe vào hệ thống
            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVMStaff can add cars to the system.");
            }

            // Thêm xe mới vào hệ thống
            CarResponse carResponse = carService.addCarToSystem(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(carResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while adding car: " + e.getMessage());
        }
    }

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

            String roleName = user.getRole_id().getRole_name();

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
}
