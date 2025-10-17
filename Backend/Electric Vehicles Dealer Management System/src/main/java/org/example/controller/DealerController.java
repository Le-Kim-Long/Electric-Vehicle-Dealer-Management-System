package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.DealerResponse;
import org.example.dto.VariantDetailResponse;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.service.DealerService;
import org.example.service.CarVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealers")
@Tag(name = "Dealer Management", description = "APIs for managing dealers with role-based access control")
public class DealerController {

    @Autowired
    private DealerService dealerService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CarVariantService carVariantService;

    @GetMapping("/names")
    @Operation(
        summary = "Get all dealer names",
        description = "Returns a list of all dealer names in the system. " +
                     "Only accessible by Admin and EVMStaff roles. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dealer names"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllDealerNames() {
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

            // Chỉ cho phép Admin và EVMStaff truy cập
            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVMStaff can view dealer information.");
            }

            List<String> dealerNames = dealerService.getAllDealerNames();
            return ResponseEntity.ok(dealerNames);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching dealer names: " + e.getMessage());
        }
    }

    @GetMapping("/details")
    @Operation(
        summary = "Get all dealer details",
        description = "Returns detailed information of all dealers in the system including ID, name, address, phone, and email. " +
                     "Only accessible by Admin and EVMStaff roles. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dealer details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllDealers() {
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

            // Chỉ cho phép Admin và EVMStaff truy cập
            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVMStaff can view dealer information.");
            }

            List<DealerResponse> dealers = dealerService.getAllDealers();
            return ResponseEntity.ok(dealers);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching dealer details: " + e.getMessage());
        }
    }

    @GetMapping("/car-variants/{dealerName}")
    @Operation(
        summary = "Get car variant details by dealer name",
        description = "Returns car variant details for a specific dealer including model name, variant name, " +
                     "available colors with prices, quantity, and image paths. " +
                     "Only accessible by Admin and EVMStaff roles. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved car variant details for dealer"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Dealer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCarVariantsByDealerName(
            @Parameter(description = "Name of the dealer to get car variants for", example = "VinFast Hanoi")
            @PathVariable String dealerName) {
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

            // Chỉ cho phép Admin và EVMStaff truy cập
            if (!"Admin".equals(roleName) && !"EVMStaff".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin and EVMStaff can view dealer car variants information.");
            }

            // Lấy car variants theo dealer name
            List<VariantDetailResponse> variantDetails = carVariantService.getVariantDetailsByDealerName(dealerName);

            if (variantDetails.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No car variants found for dealer: " + dealerName +
                          ". Please check if the dealer name is correct or if the dealer has any car variants registered.");
            }

            return ResponseEntity.ok(variantDetails);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching car variants for dealer '" + dealerName + "': " + e.getMessage());
        }
    }
}
