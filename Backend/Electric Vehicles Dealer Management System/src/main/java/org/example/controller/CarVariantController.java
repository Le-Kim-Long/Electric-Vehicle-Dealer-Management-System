package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.DealerVariantDetailResponse;
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
                     "Admin/EVMStaff see catalog with manufacturer prices, " +
                     "DealerManager see all dealer cars with dealer prices (all status), " +
                     "DealerStaff see only 'On Sale' dealer cars with dealer prices. " +
                     "Includes model name, variant name, available colors with prices, status (for dealers), and complete configuration specifications. " +
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

            String roleName = user.getRoleId().getRoleName();

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff xem xe trong catalog với giá niêm yết
                List<VariantDetailResponse> variantDetails = carVariantService.getAllVariantDetailsInSystem();
                return ResponseEntity.ok(variantDetails);

            } else if ("DealerManager".equals(roleName)) {
                // DealerManager xem tất cả xe của dealer (mọi status) với format giống admin/evmstaff nhưng có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> dealerVariantDetails = carVariantService.getDealerVariantDetailsForManager(dealerId);
                return ResponseEntity.ok(dealerVariantDetails);

            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff chỉ xem xe có status "On Sale" với format giống admin/evmstaff nhưng có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> dealerVariantDetails = carVariantService.getDealerVariantDetailsForStaff(dealerId);
                return ResponseEntity.ok(dealerVariantDetails);

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Invalid role: " + roleName);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving variant details: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search car variants based on user role",
        description = "Search car variants based on user's role: " +
                     "Admin/EVMStaff can search all variants in the system with manufacturer prices, " +
                     "DealerManager can search all dealer cars with both manufacturer prices and dealer prices (all status), " +
                     "DealerStaff can search only 'On Sale' dealer cars with dealer prices only (manufacturer prices hidden). " +
                     "Supports flexible search by model name, variant name, or combination (e.g., 'vf3', 'eco', 'vf3eco'). " +
                     "Returns detailed information including model name, variant name, available colors with prices, status (for dealers), and complete configuration specifications. " +
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

            String roleName = user.getRoleId().getRoleName();

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff tìm kiếm trong tất cả variant của hệ thống
                List<VariantDetailResponse> searchResults = carVariantService.searchVariantsInSystem(searchTerm);
                return ResponseEntity.ok(searchResults);

            } else if ("DealerManager".equals(roleName)) {
                // DealerManager tìm kiếm tất cả xe của dealer (mọi status) với format có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsForManager(dealerId, searchTerm);
                return ResponseEntity.ok(searchResults);

            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff tìm kiếm chỉ xe có status "On Sale" với format có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsForStaff(dealerId, searchTerm);
                return ResponseEntity.ok(searchResults);

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to search variants.");
            }

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
                     "Admin/EVMStaff can search all variants in the system by variant name with manufacturer prices, " +
                     "DealerManager can search all dealer cars by variant name with both manufacturer prices and dealer prices (all status), " +
                     "DealerStaff can search only 'On Sale' dealer cars by variant name with dealer prices only (manufacturer prices hidden). " +
                     "Searches only in variant names (e.g., 'eco', 'plus', 'pro') not model names. " +
                     "Returns detailed information including model name, variant name, available colors with prices, status (for dealers), and complete configuration specifications. " +
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

            String roleName = user.getRoleId().getRoleName();

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff tìm kiếm trong tất cả variant của hệ thống theo variant name
                List<VariantDetailResponse> searchResults = carVariantService.searchVariantsByVariantNameInSystem(variantName.trim());
                return ResponseEntity.ok(searchResults);

            } else if ("DealerManager".equals(roleName)) {
                // DealerManager tìm kiếm tất cả xe của dealer theo variant name (mọi status) với format có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsByVariantNameForManager(dealerId, variantName.trim());
                return ResponseEntity.ok(searchResults);

            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff tìm kiếm chỉ xe có status "On Sale" theo variant name với format có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsByVariantNameForStaff(dealerId, variantName.trim());
                return ResponseEntity.ok(searchResults);

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to search variants by variant name.");
            }

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
                     "Admin/EVMStaff can search all variants in the system by model name with manufacturer prices, " +
                     "DealerManager can search all dealer cars by model name with both manufacturer prices and dealer prices (all status), " +
                     "DealerStaff can search only 'On Sale' dealer cars by model name with dealer prices only (manufacturer prices hidden). " +
                     "Searches only in model names (e.g., 'VF3', 'VF5', 'VF8') not variant names. " +
                     "Returns detailed information including model name, variant name, available colors with prices, status (for dealers), and complete configuration specifications. " +
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
                    .body("Invalid authentication. Please login lại.");
            }

            // Lấy thông tin user để kiểm tra role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff tìm kiếm trong tất cả variant của hệ thống theo model name
                List<VariantDetailResponse> searchResults = carVariantService.searchVariantsByModelNameInSystem(modelName.trim());
                return ResponseEntity.ok(searchResults);

            } else if ("DealerManager".equals(roleName)) {
                // DealerManager tìm kiếm tất cả xe của dealer theo model name (mọi status) với format có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsByModelNameForManager(dealerId, modelName.trim());
                return ResponseEntity.ok(searchResults);

            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff tìm kiếm chỉ xe có status "On Sale" theo model name với format có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsByModelNameForStaff(dealerId, modelName.trim());
                return ResponseEntity.ok(searchResults);

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to search variants by model name.");
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching variants by model name: " + e.getMessage());
        }
    }

    @GetMapping("/search/model-and-variant")
    @Operation(
        summary = "Search car variants by both model name and variant name based on user role",
        description = "Search car variants by both model name and variant name simultaneously based on user's role: " +
                     "Admin/EVMStaff can search all variants in the system by both criteria with manufacturer prices, " +
                     "DealerManager can search all dealer cars by both criteria with both manufacturer prices and dealer prices (all status), " +
                     "DealerStaff can search only 'On Sale' dealer cars by both criteria with dealer prices only (manufacturer prices hidden). " +
                     "This API is designed for UI with two select boxes where users can filter by both model and variant. " +
                     "Example: Search for modelName='VF3' and variantName='eco' to get VF3 Eco variants. " +
                     "Returns detailed information including model name, variant name, available colors with prices, status (for dealers), and complete configuration specifications. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results by both model and variant name"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid model name or variant name"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchVariantsByModelAndVariantName(
            @Parameter(description = "Model name to filter variants. Example: VF3, VF5, VF8",
                      example = "VF3", required = true)
            @RequestParam("modelName") String modelName,
            @Parameter(description = "Variant name to filter variants. Example: eco, plus, pro",
                      example = "eco", required = true)
            @RequestParam("variantName") String variantName) {
        try {
            // Validate model name
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Model name is required and cannot be empty");
            }

            // Validate variant name
            if (variantName == null || variantName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Variant name is required and cannot be empty");
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
                    .body("Invalid authentication. Please login lại.");
            }

            // Lấy thông tin user để kiểm tra role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Phân quyền dựa trên role
            if ("Admin".equals(roleName) || "EVMStaff".equals(roleName)) {
                // Admin và EVMStaff tìm kiếm trong tất cả variant của hệ thống theo cả model name và variant name
                List<VariantDetailResponse> searchResults = carVariantService.searchVariantsByModelAndVariantNameInSystem(modelName.trim(), variantName.trim());
                return ResponseEntity.ok(searchResults);

            } else if ("DealerManager".equals(roleName)) {
                // DealerManager tìm kiếm tất cả xe của dealer theo cả model name và variant name (mọi status) với format có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsByModelAndVariantNameForManager(dealerId, modelName.trim(), variantName.trim());
                return ResponseEntity.ok(searchResults);

            } else if ("DealerStaff".equals(roleName)) {
                // DealerStaff tìm kiếm chỉ xe có status "On Sale" theo cả model name và variant name với format có thêm status
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsByModelAndVariantNameForStaff(dealerId, modelName.trim(), variantName.trim());
                return ResponseEntity.ok(searchResults);

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Your role does not have permission to search variants by model and variant name.");
            }


        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching variants by model and variant name: " + e.getMessage());
        }
    }

    @GetMapping("/variant-names")
    @Operation(summary = "Get all variant names", description = "Retrieve all available variant names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved variant names"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getAllVariantNames() {
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

            List<String> variantNames = carVariantService.getAllVariantNames();
            return ResponseEntity.ok(variantNames);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while retrieving variant names: " + e.getMessage());
        }
    }

    @GetMapping("/variant-names/by-model")
    @Operation(summary = "Get variant names by model name", description = "Retrieve all variant names for a specific car model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved variant names for the model"),
            @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid model name"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Model not found or no variants available"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getVariantNamesByModelName(
            @Parameter(description = "Model name to get variant names for", required = true, example = "VF3")
            @RequestParam String modelName) {
        try {
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Model name is required and cannot be empty");
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
                    .body("Invalid authentication. Please login lại.");
            }

            // Lấy thông tin user để kiểm tra role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            List<String> variantNames = carVariantService.getVariantNamesByModelName(modelName.trim());

            if (variantNames.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No variants found for model: " + modelName);
            }

            return ResponseEntity.ok(variantNames);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while retrieving variant names for model " + modelName + ": " + e.getMessage());
        }
    }

    @GetMapping("/description")
    @Operation(summary = "Get description by model name and variant name", description = "Retrieve description information for a specific model and variant combination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved description"),
            @ApiResponse(responseCode = "404", description = "Model-Variant combination not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid model name or variant name"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getDescriptionByModelNameAndVariantName(
            @Parameter(description = "Model name to get description for", required = true, example = "VF3")
            @RequestParam String modelName,
            @Parameter(description = "Variant name to get description for", required = true, example = "eco")
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


            String description = carVariantService.getDescriptionByModelNameAndVariantName(modelName.trim(), variantName.trim());
            if (description != null && !description.trim().isEmpty()) {
                return ResponseEntity.ok(description);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No description found for model '" + modelName + "' and variant '" + variantName + "'");
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while retrieving variant description: " + e.getMessage());
        }
    }

    @GetMapping("/search/status")
    @Operation(
        summary = "Search car variants by status - Dealer Manager only",
        description = "Search car variants by status. This API is exclusively for Dealer Managers to filter their inventory by status. " +
                     "Common status values include 'On Sale', 'Pending', 'Out of Stock', etc. " +
                     "Returns detailed information including model name, variant name, available colors with both manufacturer prices and dealer prices, status, quantity, and complete configuration specifications. " +
                     "Only Dealer Manager role can access this API. Dealer Staff should use regular search APIs which automatically filter by 'On Sale' status. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results by status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid status"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or not a Dealer Manager"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchVariantsByStatus(
            @Parameter(description = "Status to filter variants. Example: On Sale, Pending, Out of Stock",
                      example = "On Sale", required = true)
            @RequestParam("status") String status) {
        try {
            // Validate status
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Status is required and cannot be empty");
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

            String roleName = user.getRoleId().getRoleName();

            // Chỉ cho phép DealerManager sử dụng API này
            if ("DealerManager".equals(roleName)) {
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();
                List<DealerVariantDetailResponse> searchResults = carVariantService.searchDealerVariantsByStatusForManager(dealerId, status.trim());
                return ResponseEntity.ok(searchResults);

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. This API is only available for Dealer Manager role. " +
                          "Current role: " + roleName);
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching variants by status: " + e.getMessage());
        }
    }

    @PutMapping("/update-dealer-car")
    @Operation(
        summary = "Update dealer price and status - Dealer Manager only",
        description = "Update dealer price and status for a specific car variant by model name, variant name, and color name. " +
                     "This API is exclusively for Dealer Managers to manage their inventory pricing and availability status. " +
                     "The API will find the matching car based on the provided model name, variant name, and color name, " +
                     "then update the dealer price and status for that specific car in the dealer's inventory. " +
                     "Only Dealer Manager role can access this API. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated dealer price and status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid parameters"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or not a Dealer Manager"),
        @ApiResponse(responseCode = "404", description = "Car not found in dealer inventory"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateDealerCarPriceAndStatus(
            @Parameter(description = "Model name of the car. Example: VF3, VF5, VF8",
                      example = "VF3", required = true)
            @RequestParam("modelName") String modelName,
            @Parameter(description = "Variant name of the car. Example: eco, plus, pro",
                      example = "eco", required = true)
            @RequestParam("variantName") String variantName,
            @Parameter(description = "Color name of the car. Example: Đỏ, Xanh, Trắng",
                      example = "Đỏ", required = true)
            @RequestParam("colorName") String colorName,
            @Parameter(description = "New dealer price in VND. Must be a positive number",
                      example = "750000000", required = true)
            @RequestParam("dealerPrice") String dealerPrice,
            @Parameter(description = "New status. Example: On Sale, Pending, Out of Stock",
                      example = "On Sale", required = true)
            @RequestParam("status") String status) {
        try {
            // Validate parameters
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
            if (dealerPrice == null || dealerPrice.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dealer price is required and cannot be empty");
            }
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Status is required and cannot be empty");
            }

            // Validate dealer price format
            java.math.BigDecimal dealerPriceValue;
            try {
                dealerPriceValue = new java.math.BigDecimal(dealerPrice.trim());
                if (dealerPriceValue.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Dealer price must be a positive number");
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid dealer price format. Please provide a valid number");
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
                    .body("Invalid authentication. Please login lại.");
            }

            // Lấy thông tin user để kiểm tra role
            UserAccount user = userAccountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String roleName = user.getRoleId().getRoleName();

            // Chỉ cho phép DealerManager sử dụng API này
            if ("DealerManager".equals(roleName)) {
                if (user.getDealer() == null) {
                    throw new RuntimeException("User not associated with any dealer");
                }
                Integer dealerId = user.getDealer().getDealerId();

                // Call service to update dealer car price and status
                boolean updated = carVariantService.updateDealerCarPriceAndStatus(
                    dealerId, modelName.trim(), variantName.trim(), colorName.trim(),
                    dealerPriceValue, status.trim());

                if (updated) {
                    return ResponseEntity.ok("Successfully updated dealer price and status for " +
                        modelName + " " + variantName + " (" + colorName + ")");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Car not found in dealer inventory: " + modelName + " " + variantName + " (" + colorName + ")");
                }

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. This API is only available for Dealer Manager role. " +
                          "Current role: " + roleName);
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating dealer car price and status: " + e.getMessage());
        }
    }

    @GetMapping("/not-available-at-dealer")
    @Operation(
        summary = "Get cars not available at dealer - Dealer Manager only",
        description = "Retrieve all car variants that are not yet available at the current dealer manager's dealer. " +
                     "This API is exclusively for Dealer Managers to see what cars they can potentially add to their inventory. " +
                     "Returns detailed information including model name, variant name, available colors with manufacturer prices, " +
                     "and complete configuration specifications. Quantity is set to 0 since these cars are not available at the dealer. " +
                     "Only Dealer Manager role can access this API. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cars not available at dealer"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions or not a Dealer Manager"),
        @ApiResponse(responseCode = "404", description = "User not associated with any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCarsNotAvailableAtDealer() {
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

            // Chỉ cho phép DealerManager sử dụng API này
            if ("DealerManager".equals(roleName)) {
                if (user.getDealer() == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not associated with any dealer");
                }

                Integer dealerId = user.getDealer().getDealerId();
                List<VariantDetailResponse> carsNotAvailable = carVariantService.getCarsNotAvailableAtDealer(dealerId);

                return ResponseEntity.ok(carsNotAvailable);

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. This API is only available for Dealer Manager role. " +
                          "Current role: " + roleName);
            }

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("not associated")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while retrieving cars not available at dealer: " + e.getMessage());
        }
    }
}
