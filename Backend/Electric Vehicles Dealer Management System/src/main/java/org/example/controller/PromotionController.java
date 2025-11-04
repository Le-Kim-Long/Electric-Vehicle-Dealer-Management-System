package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.CreatePromotionRequest;
import org.example.dto.PromotionResponse;
import org.example.dto.UpdatePromotionRequest;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@Tag(name = "Promotion Management", description = "APIs for managing promotions with role-based access control")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping
    @Operation(
        summary = "Get all promotions for dealer manager's dealer",
        description = "Retrieves all promotions belonging to the dealer that the logged-in dealer manager is associated with. " +
                     "Only Dealer Manager role can access this endpoint. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved promotions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Dealer Manager can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "User not found or not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllPromotionsForDealerManager() {
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

            // Check user role and dealer association
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Only Dealer Manager can access this endpoint
            if (roleName.equals("EVMStaff") || roleName.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Dealer Manager can access promotions.");
            }

            // Check if user is associated with a dealer
            if (user.getDealer() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User is not associated with any dealer.");
            }

            // Get all promotions for the dealer
            List<PromotionResponse> promotions = promotionService.getPromotionsByDealerManager(email);

            if (promotions.isEmpty()) {
                return ResponseEntity.ok("No promotions found for dealer: " + user.getDealer().getDealerName());
            }

            return ResponseEntity.ok(promotions);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("not associated")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving promotions: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get promotion by ID for dealer manager's dealer",
        description = "Retrieves a specific promotion by ID if it belongs to the dealer that the logged-in dealer manager is associated with. " +
                     "Only Dealer Manager role can access this endpoint. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved promotion"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Dealer Manager can access this endpoint or promotion doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Promotion not found or user not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getPromotionById(@PathVariable("id") Integer promotionId) {
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

            // Check user role and dealer association
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Only Dealer Manager can access this endpoint
            if (roleName.equals("EVMStaff") || roleName.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Dealer Manager and Dealer Staff can access promotions.");
            }

            // Check if user is associated with a dealer
            if (user.getDealer() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User is not associated with any dealer.");
            }

            // Get promotion by ID for the dealer
            PromotionResponse promotion = promotionService.getPromotionByIdForDealerManager(promotionId, email);

            return ResponseEntity.ok(promotion);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            if (e.getMessage().contains("Access denied") || e.getMessage().contains("does not belong")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving promotion: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update promotion by ID for dealer manager's dealer",
        description = "Updates a specific promotion by ID if it belongs to the dealer that the logged-in dealer manager is associated with. " +
                     "Only Dealer Manager role can access this endpoint. " +
                     "Requires JWT token in Authorization header. " +
                     "Business Rules for Date Updates: " +
                     "- If promotion has started (current date >= start date): Only end date can be updated " +
                     "- If promotion hasn't started (current date < start date): Both start and end dates can be updated " +
                     "- Status is automatically calculated based on current date vs promotion dates (cannot be manually updated) " +
                     "- Updatable fields: promotionName, description, value, type, scope, startDate (with restrictions), endDate (with restrictions) " +
                     "- Non-updatable fields: status (auto-calculated)"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated promotion with automatic status update"),
        @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., trying to update start date after promotion started, invalid date ranges)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Dealer Manager can access this endpoint or promotion doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Promotion not found or user not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updatePromotionById(
            @PathVariable("id") Integer promotionId,
            @RequestBody UpdatePromotionRequest updateRequest) {
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

            // Check user role and dealer association
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Only Dealer Manager can access this endpoint
            if (!"DealerManager".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Dealer Manager can update promotions.");
            }

            // Check if user is associated with a dealer
            if (user.getDealer() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User is not associated with any dealer.");
            }

            // Update promotion for the dealer
            PromotionResponse updatedPromotion = promotionService.updatePromotionForDealerManager(promotionId, updateRequest, email);

            return ResponseEntity.ok(updatedPromotion);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            if (e.getMessage().contains("Access denied") || e.getMessage().contains("does not belong")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }
            if (e.getMessage().contains("Cannot update start date") ||
                e.getMessage().contains("Start date must be after current date") ||
                e.getMessage().contains("End date must be after") ||
                e.getMessage().contains("after promotion has started")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating promotion: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(
        summary = "Create new promotion for dealer manager's dealer",
        description = "Creates a new promotion for the dealer that the logged-in dealer manager is associated with. " +
                     "Only Dealer Manager role can access this endpoint. " +
                     "Requires JWT token in Authorization header. " +
                     "Business Rules: " +
                     "- Start date must be after current date " +
                     "- End date must be after start date " +
                     "- Status is automatically set: 'Đang hoạt động' if current date is between start and end dates, otherwise 'Ngừng hoạt động'"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created promotion"),
        @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., invalid dates, missing required fields)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Dealer Manager can create promotions"),
        @ApiResponse(responseCode = "404", description = "User not found or not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createPromotion(@RequestBody CreatePromotionRequest createRequest) {
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

            // Validate required fields
            if (createRequest.getPromotionName() == null || createRequest.getPromotionName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Promotion name is required");
            }
            if (createRequest.getValue() == null || createRequest.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Value is required and must be greater than 0");
            }
            if (createRequest.getType() == null || createRequest.getType().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Type is required");
            }
            if (createRequest.getStartDate() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Start date is required");
            }
            if (createRequest.getEndDate() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("End date is required");
            }

            // Check user role and dealer association
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Only Dealer Manager can access this endpoint
            if (!"DealerManager".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Dealer Manager can create promotions.");
            }

            // Check if user is associated with a dealer
            if (user.getDealer() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User is not associated with any dealer.");
            }

            // Create promotion for the dealer
            PromotionResponse createdPromotion = promotionService.createPromotionForDealerManager(createRequest, email);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdPromotion);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            if (e.getMessage().contains("Start date must be after current date") ||
                e.getMessage().contains("End date must be after start date") ||
                e.getMessage().contains("required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating promotion: " + e.getMessage());
        }
    }

    @GetMapping("/search/status")
    @Operation(
        summary = "Search promotions by status for dealer manager's dealer",
        description = "Searches for promotions with specific status belonging to the dealer that the logged-in dealer manager is associated with. " +
                     "Only Dealer Manager role can access this endpoint. " +
                     "Requires JWT token in Authorization header. " +
                     "Valid status values: 'Đang hoạt động', 'Không hoạt động'"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved promotions matching the status"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing status parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Dealer Manager can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "User not found or not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchPromotionsByStatus(@RequestParam("status") String status) {
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

            // Validate status parameter
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Status parameter is required. Valid values: 'Đang hoạt động', 'Không hoạt động'");
            }

            // Check user role and dealer association
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Only Dealer Manager can access this endpoint
            if (!"DealerManager".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Dealer Manager can search promotions.");
            }

            // Check if user is associated with a dealer
            if (user.getDealer() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User is not associated with any dealer.");
            }

            // Search promotions by status
            List<PromotionResponse> promotions = promotionService.searchPromotionsByStatus(email, status);

            if (promotions.isEmpty()) {
                return ResponseEntity.ok("No promotions found with status: '" + status + "' for dealer: " + user.getDealer().getDealerName());
            }

            return ResponseEntity.ok(promotions);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("not associated")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            if (e.getMessage().contains("Status parameter is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while searching promotions by status: " + e.getMessage());
        }
    }

    @GetMapping("/search/type")
    @Operation(
        summary = "Search promotions by type for dealer manager's dealer",
        description = "Searches for promotions with specific type belonging to the dealer that the logged-in dealer manager is associated with. " +
                     "Only Dealer Manager role can access this endpoint. " +
                     "Requires JWT token in Authorization header. " +
                     "Search is case-insensitive."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved promotions matching the type"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing type parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Dealer Manager can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "User not found or not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchPromotionsByType(@RequestParam("type") String type) {
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

            // Validate type parameter
            if (type == null || type.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Type parameter is required");
            }

            // Check user role and dealer association
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Only Dealer Manager can access this endpoint
            if (!"DealerManager".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Dealer Manager can search promotions.");
            }

            // Check if user is associated with a dealer
            if (user.getDealer() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User is not associated with any dealer.");
            }

            // Search promotions by type
            List<PromotionResponse> promotions = promotionService.searchPromotionsByType(email, type);

            if (promotions.isEmpty()) {
                return ResponseEntity.ok("No promotions found with type: '" + type + "' for dealer: " + user.getDealer().getDealerName());
            }

            return ResponseEntity.ok(promotions);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("not associated")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            if (e.getMessage().contains("Type parameter is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while searching promotions by type: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search promotions by multiple criteria for dealer manager's dealer",
        description = "Searches for promotions using multiple optional criteria (type, scope, status) belonging to the dealer that the logged-in dealer manager is associated with. " +
                     "Only Dealer Manager role can access this endpoint. " +
                     "Requires JWT token in Authorization header. " +
                     "All parameters are optional - can search by any combination of type, scope, and status. " +
                     "If no parameters are provided, returns all promotions for the dealer. " +
                     "Search is case-insensitive for all text parameters."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved promotions matching the criteria"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Dealer Manager can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "User not found or not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchPromotionsMultiCriteria(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "status", required = false) String status) {
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

            // Check user role and dealer association
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Only Dealer Manager can access this endpoint
            if (!"DealerManager".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Dealer Manager can search promotions.");
            }

            // Check if user is associated with a dealer
            if (user.getDealer() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User is not associated with any dealer.");
            }

            // Search promotions using multiple criteria
            List<PromotionResponse> promotions = promotionService.searchPromotionsMultiCriteria(email, type, status);

            if (promotions.isEmpty()) {
                StringBuilder searchCriteria = new StringBuilder();
                if (type != null && !type.trim().isEmpty()) {
                    searchCriteria.append("type='").append(type).append("' ");
                }
                if (scope != null && !scope.trim().isEmpty()) {
                    searchCriteria.append("scope='").append(scope).append("' ");
                }
                if (status != null && !status.trim().isEmpty()) {
                    searchCriteria.append("status='").append(status).append("' ");
                }

                String criteriaText = searchCriteria.length() > 0 ?
                    "with criteria: " + searchCriteria.toString().trim() :
                    "with no specific criteria";

                return ResponseEntity.ok("No promotions found " + criteriaText + " for dealer: " + user.getDealer().getDealerName());
            }

            return ResponseEntity.ok(promotions);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("not associated")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while searching promotions: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete promotion by ID for dealer manager's dealer",
        description = "Deletes a specific promotion by ID if it belongs to the dealer that the logged-in dealer manager is associated with. " +
                     "Only Dealer Manager role can access this endpoint. " +
                     "Requires JWT token in Authorization header. " +
                     "Business Rules: " +
                     "- Only promotions that haven't started yet (current date < start date) can be deleted " +
                     "- Active or expired promotions cannot be deleted to maintain data integrity"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted promotion"),
        @ApiResponse(responseCode = "400", description = "Bad request - Cannot delete active or expired promotions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Dealer Manager can access this endpoint or promotion doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Promotion not found or user not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deletePromotionById(@PathVariable("id") Integer promotionId) {
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

            // Check user role and dealer association
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Only Dealer Manager can access this endpoint
            if (!"DealerManager".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Dealer Manager can delete promotions.");
            }

            // Check if user is associated with a dealer
            if (user.getDealer() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User is not associated with any dealer.");
            }

            // Delete promotion by ID for the dealer
            String result = promotionService.deletePromotionForDealerManager(promotionId, email);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }
            if (e.getMessage().contains("Access denied") || e.getMessage().contains("does not belong")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage());
            }
            if (e.getMessage().contains("Cannot delete") ||
                e.getMessage().contains("has already started") ||
                e.getMessage().contains("has expired")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting promotion: " + e.getMessage());
        }
    }

}
