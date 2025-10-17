package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.CreateUserAccountRequest;
import org.example.dto.RoleResponse;
import org.example.dto.UpdateUserAccountRequest;
import org.example.dto.UserAccountResponse;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "User Account Management", description = "APIs for managing user accounts - Admin only")
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @GetMapping("")
    @Operation(
        summary = "Get all user accounts",
        description = "Returns a list of all user accounts in the system with role and dealer information. " +
                     "Only accessible by Admin role. " +
                     "Includes username, email, phone, status, creation date, role name, and dealer information. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all user accounts"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin can access this endpoint"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllUsers() {
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

            // Chỉ cho phép Admin truy cập
            if (!"Admin".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin can view all user accounts.");
            }

            List<UserAccountResponse> users = userAccountService.getAllUsers();
            return ResponseEntity.ok(users);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching user accounts: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    @Operation(
        summary = "Get user account by ID",
        description = "Returns detailed information of a specific user account by ID. " +
                     "Only accessible by Admin role. " +
                     "This API is typically used to pre-fill form data for user updates. " +
                     "Includes all user information: username, email, phone, status, role, and dealer details. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user account details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "User not found with the specified ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserById(
            @Parameter(description = "User ID to retrieve", required = true, example = "9")
            @PathVariable Integer userId) {
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

            // Chỉ cho phép Admin truy cập
            if (!"Admin".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin can view user account details.");
            }

            // Lấy thông tin user theo ID
            UserAccountResponse userDetails = userAccountService.getUserById(userId);
            return ResponseEntity.ok(userDetails);

        } catch (RuntimeException e) {
            // Xử lý lỗi user not found
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching user account: " + e.getMessage());
        }
    }

    @GetMapping("/roles")
    @Operation(
        summary = "Get all roles",
        description = "Returns a list of all roles available in the system. " +
                     "Only accessible by Admin role. " +
                     "This API is typically used to populate role dropdown in user management forms. " +
                     "Includes role ID and role name for each role. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all roles"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin can access this endpoint"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllRoles() {
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

            // Chỉ cho phép Admin truy cập
            if (!"Admin".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin can view all roles.");
            }

            // Lấy tất cả roles
            List<RoleResponse> roles = userAccountService.getAllRoles();
            return ResponseEntity.ok(roles);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching roles: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search user accounts",
        description = "Search user accounts by keyword in username, email, role name, or dealer name. " +
                     "Only accessible by Admin role. " +
                     "Returns matching users with complete information including role and dealer details. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved matching user accounts"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin can access this endpoint"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid search keyword"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchUsers(
            @Parameter(description = "Search keyword to find in username, email, role name, or dealer name",
                      example = "admin", required = true)
            @RequestParam String keyword) {
        try {
            // Kiểm tra keyword có hợp lệ không
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Search keyword is required and cannot be empty.");
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

            // Chỉ cho phép Admin truy cập
            if (!"Admin".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin can search user accounts.");
            }

            List<UserAccountResponse> users = userAccountService.searchUsers(keyword.trim());

            if (users.isEmpty()) {
                return ResponseEntity.ok().body("No users found matching keyword: " + keyword);
            }

            return ResponseEntity.ok(users);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching user accounts: " + e.getMessage());
        }
    }

    @PostMapping("")
    @Operation(
        summary = "Create new user account",
        description = "Creates a new user account with specified role and dealer assignment. " +
                     "Only accessible by Admin role. " +
                     "Valid roles are: DealerStaff, EVMStaff, DealerManager. " +
                     "For DealerStaff and DealerManager roles, dealerName is required. " +
                     "For EVMStaff role, no dealer assignment needed. " +
                     "Use /api/dealers/names to get list of available dealer names. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User account created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin can create user accounts"),
        @ApiResponse(responseCode = "409", description = "Conflict - Username or email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createUserAccount(
            @Parameter(description = "User account creation request with all required information. " +
                      "For DealerStaff and DealerManager roles, provide dealerName (not dealerId)",
                      required = true)
            @RequestBody CreateUserAccountRequest request) {
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

            // Chỉ cho phép Admin tạo user account
            if (!"Admin".equals(roleName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin can create new user accounts.");
            }

            // Tạo user account mới
            UserAccountResponse newUser = userAccountService.createUserAccount(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);

        } catch (RuntimeException e) {
            // Kiểm tra lỗi conflict (username hoặc email đã tồn tại)
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while creating user account: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    @Operation(
        summary = "Update user account",
        description = "Updates an existing user account by ID. " +
                     "Only accessible by Admin role. " +
                     "All fields are optional - only provided fields will be updated. " +
                     "For DealerStaff and DealerManager roles, dealerName must be provided. " +
                     "EVMStaff role should not have dealerName. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User account updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "409", description = "Conflict - Username or email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateUserAccount(
            @Parameter(description = "User ID to update", required = true)
            @PathVariable Integer userId,
            @Parameter(description = "User account update data", required = true)
            @RequestBody UpdateUserAccountRequest request) {
        try {
            // Lấy authentication từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName(); // JWT token lưu email, không phải username

            // Tìm user hiện tại để kiểm tra role (tìm bằng email thay vì username)
            UserAccount currentUser = userAccountRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("Current user not found with email: " + currentUserEmail));

            // Kiểm tra quyền Admin
            if (!"Admin".equals(currentUser.getRole_id().getRole_name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Only Admin can update user accounts.");
            }

            // Cập nhật user account
            UserAccountResponse updatedUser = userAccountService.updateUserAccount(userId, request);

            return ResponseEntity.ok(updatedUser);

        } catch (RuntimeException e) {
            // Xử lý các lỗi cụ thể
            String errorMessage = e.getMessage();

            if (errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            } else if (errorMessage.contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
            } else if (errorMessage.contains("Cannot update Admin account") ||
                      errorMessage.contains("protected from modifications")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage);
            } else if (errorMessage.contains("required") ||
                      errorMessage.contains("Invalid") ||
                      errorMessage.contains("must be")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user account: " + e.getMessage());
        }
    }

    @GetMapping("/search/role")
    @Operation(
        summary = "Search user accounts by role name",
        description = "Search user accounts by specific role name. " +
                     "Only accessible by Admin role. " +
                     "Valid role names are: Admin, DealerStaff, EVMStaff, DealerManager. " +
                     "Returns all users with the specified role including complete information. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user accounts for the specified role"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin can access this endpoint"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid role name"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchUsersByRole(
            @Parameter(description = "Role name to search for users. Valid values: Admin, DealerStaff, EVMStaff, DealerManager",
                      example = "DealerStaff", required = true)
            @RequestParam String roleName) {
        try {
            // Kiểm tra roleName có hợp lệ không
            if (roleName == null || roleName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Role name is required and cannot be empty.");
            }

            // Validate role name
            String trimmedRoleName = roleName.trim();
            if (!"Admin".equals(trimmedRoleName) &&
                !"DealerStaff".equals(trimmedRoleName) &&
                !"EVMStaff".equals(trimmedRoleName) &&
                !"DealerManager".equals(trimmedRoleName)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid role name. Valid role names are: Admin, DealerStaff, EVMStaff, DealerManager");
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

            String currentUserRole = user.getRole_id().getRole_name();

            // Chỉ cho phép Admin truy cập
            if (!"Admin".equals(currentUserRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin can search user accounts by role.");
            }

            List<UserAccountResponse> users = userAccountService.searchUsersByRole(trimmedRoleName);

            if (users.isEmpty()) {
                return ResponseEntity.ok().body("No users found with role: " + trimmedRoleName);
            }

            return ResponseEntity.ok(users);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching user accounts by role: " + e.getMessage());
        }
    }

    @GetMapping("/search/dealer")
    @Operation(
        summary = "Search user accounts by dealer name",
        description = "Search user accounts by specific dealer name. " +
                     "Only accessible by Admin role. " +
                     "Returns all users associated with the specified dealer including complete information. " +
                     "Only returns users who have dealer assignments (DealerStaff and DealerManager roles). " +
                     "Use /api/dealers/names to get list of available dealer names. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user accounts for the specified dealer"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only Admin can access this endpoint"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing or invalid dealer name"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchUsersByDealer(
            @Parameter(description = "Dealer name to search for users. Must be exact dealer name as registered in system",
                      example = "VinFast Đại lý Quận 1", required = true)
            @RequestParam String dealerName) {
        try {
            // Kiểm tra dealerName có hợp lệ không
            if (dealerName == null || dealerName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Dealer name is required and cannot be empty.");
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

            String currentUserRole = user.getRole_id().getRole_name();

            // Chỉ cho phép Admin truy cập
            if (!"Admin".equals(currentUserRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied. Only Admin can search user accounts by dealer.");
            }

            List<UserAccountResponse> users = userAccountService.searchUsersByDealer(dealerName.trim());

            if (users.isEmpty()) {
                return ResponseEntity.ok().body("No users found for dealer: " + dealerName);
            }

            return ResponseEntity.ok(users);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while searching user accounts by dealer: " + e.getMessage());
        }
    }
}
