package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for login and authentication")
public class AuthControllerAPI {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates user with email and password. " +
                     "Returns JWT token and user information on successful login. " +
                     "Provides specific error messages for different failure scenarios: " +
                     "- Email not found: 'Account does not exist' " +
                     "- Wrong password: 'Incorrect password' " +
                     "- Account suspended: 'Account is not active or has been suspended'"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful - Returns JWT token and user info"),
        @ApiResponse(responseCode = "400", description = "Bad request - Missing email or password"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Account does not exist or incorrect password"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Account is not active or suspended"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> login(
            @Parameter(description = "Login credentials with email and password", required = true)
            @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            // Trả về thông báo lỗi cụ thể với status code phù hợp
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Xử lý các lỗi không mong đợi khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during login: " + e.getMessage());
        }
    }
}
