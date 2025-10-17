package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.VariantDetailResponse;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.service.CarVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/car-variants")
@Tag(name = "Car Variant Management", description = "APIs for managing car variants with role-based access control")
public class CarVariantController {

    @Autowired
    private CarVariantService carVariantService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("/details")
    @Operation(
        summary = "Get car variant details based on user role",
        description = "Returns car variant details based on user's role: " +
                     "Admin/EVMStaff can see all variants in the system, " +
                     "DealerStaff can only see variants available at their dealer. " +
                     "Includes model name, variant name, available colors with prices, and complete configuration specifications. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved variant details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getVariantDetails() {
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

            List<VariantDetailResponse> variantDetails;

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff xem tất cả variant trong hệ thống
                variantDetails = carVariantService.getAllVariantDetailsInSystem();
            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff chỉ xem variant của dealer mình
                variantDetails = carVariantService.getVariantDetailsByCurrentDealer(email);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to view variant details.");
            }

            return ResponseEntity.ok(variantDetails);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching variant details: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search car variants based on user role",
        description = "Search car variants based on user's role: " +
                     "Admin/EVMStaff can search all variants in the system, " +
                     "DealerStaff can only search variants available at their dealer. " +
                     "Supports flexible search by model name, variant name, or combination (e.g., 'vf3', 'eco', 'vf3eco'). " +
                     "Returns detailed information including model name, variant name, available colors with prices, and complete configuration specifications. " +
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
    public ResponseEntity<?> searchVariants(@RequestParam("q") String searchTerm) {
        try {
            // Validate search term
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

            List<VariantDetailResponse> searchResults;

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff tìm kiếm trong tất cả variant của hệ thống
                searchResults = carVariantService.searchVariantsInSystem(searchTerm);
            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff chỉ tìm kiếm trong variant của dealer mình
                searchResults = carVariantService.searchVariantsByCurrentDealer(email, searchTerm);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to search variants.");
            }

            return ResponseEntity.ok(searchResults);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching variants: " + e.getMessage());
        }
    }

    @GetMapping("/search/variant-name")
    @Operation(
        summary = "Search car variants by variant name based on user role",
        description = "Search car variants by specific variant name based on user's role: " +
                     "Admin/EVMStaff can search all variants in the system by variant name, " +
                     "DealerStaff can only search variants available at their dealer by variant name. " +
                     "Searches only in variant names (e.g., 'eco', 'plus', 'pro') not model names. " +
                     "Returns detailed information including model name, variant name, available colors with prices, and complete configuration specifications. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results by variant name"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid variant name"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchVariantsByVariantName(@RequestParam("variantName") String variantName) {
        try {
            // Validate variant name
            if (variantName == null || variantName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Variant name cannot be empty");
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

            List<VariantDetailResponse> searchResults;

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff tìm kiếm trong tất cả variant của hệ thống theo variant name
                searchResults = carVariantService.searchVariantsByVariantNameInSystem(variantName.trim());
            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff chỉ tìm kiếm trong variant của dealer mình theo variant name
                searchResults = carVariantService.searchVariantsByVariantNameAndCurrentDealer(email, variantName.trim());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to search variants by variant name.");
            }

            return ResponseEntity.ok(searchResults);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching variants by variant name: " + e.getMessage());
        }
    }

    @GetMapping("/search/model-name")
    @Operation(
        summary = "Search car variants by model name based on user role",
        description = "Search car variants by specific model name based on user's role: " +
                     "Admin/EVMStaff can search all variants in the system by model name, " +
                     "DealerStaff can only search variants available at their dealer by model name. " +
                     "Searches only in model names (e.g., 'VF3', 'VF5', 'VF8') not variant names. " +
                     "Returns detailed information including model name, variant name, available colors with prices, and complete configuration specifications. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results by model name"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid model name"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchVariantsByModelName(@RequestParam("modelName") String modelName) {
        try {
            // Validate model name
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Model name cannot be empty");
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

            List<VariantDetailResponse> searchResults;

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff tìm kiếm trong tất cả variant của hệ thống theo model name
                searchResults = carVariantService.searchVariantsByModelNameInSystem(modelName.trim());
            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff chỉ tìm kiếm trong variant của dealer mình theo model name
                searchResults = carVariantService.searchVariantsByModelNameAndCurrentDealer(email, modelName.trim());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to search variants by model name.");
            }

            return ResponseEntity.ok(searchResults);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching variants by model name: " + e.getMessage());
        }
    }
}
