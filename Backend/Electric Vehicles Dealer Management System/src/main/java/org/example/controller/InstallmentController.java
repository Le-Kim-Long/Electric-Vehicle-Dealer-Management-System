package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.CreateInstallmentRequest;
import org.example.dto.CreateInstallmentResponse;
import org.example.dto.UpdateInstallmentRequest;
import org.example.dto.UpdateInstallmentResponse;
import org.example.service.InstallmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dealer/installments")
@Tag(name = "Dealer Installment Management", description = "APIs for managing installment plans - DealerManager and DealerStaff only")
public class InstallmentController {

    @Autowired
    private InstallmentService installmentService;

    @PostMapping("")
    @Operation(
        summary = "Create installment plan",
        description = "Creates a new installment plan for an order. " +
                     "Only dealer manager and dealer staff can create installment plans. " +
                     "The principal amount is automatically taken from the order's total amount. " +
                     "Calculates total interest, total pay amount, and amount per term automatically. " +
                     "Order must belong to the current user's dealer and have payment method set to 'Installment' or 'Trả góp'. " +
                     "Order cannot already have an existing installment plan."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Installment plan created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data, order already has installment plan, or order total amount is zero"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can create installment plans"),
        @ApiResponse(responseCode = "404", description = "Order not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "409", description = "Conflict - Order payment method is not set to installment"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createInstallment(
            @Parameter(description = "Installment plan creation details", required = true)
            @Valid @RequestBody CreateInstallmentRequest request) {
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

            // Tạo installment plan
            CreateInstallmentResponse response = installmentService.createInstallment(request, email);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("already has an installment plan")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } else if (e.getMessage().contains("payment method must be set") ||
                      e.getMessage().contains("must have a total amount")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            } else if (e.getMessage().contains("is required") ||
                      e.getMessage().contains("must be") ||
                      e.getMessage().contains("cannot be")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while creating installment plan: " + e.getMessage());
        }
    }

    @PutMapping("/order/{orderId}")
    @Operation(
        summary = "Update installment plan by order ID",
        description = "Updates an existing installment plan for a specific order ID. " +
                     "Only dealer manager and dealer staff can update installment plans. " +
                     "Updates term count, interest rate, and recalculates all financial amounts automatically. " +
                     "The principal amount remains the same (from order's total amount). " +
                     "Order must belong to the current user's dealer and must have an existing installment plan."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Installment plan updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can update installment plans"),
        @ApiResponse(responseCode = "404", description = "Order not found, no installment plan found, or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateInstallmentByOrderId(
            @Parameter(description = "Order ID to update installment plan for", required = true)
            @PathVariable Integer orderId,
            @Parameter(description = "Installment plan update details", required = true)
            @Valid @RequestBody UpdateInstallmentRequest request) {
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

            // Cập nhật installment plan theo order ID
            UpdateInstallmentResponse response = installmentService.updateInstallmentByOrderId(orderId, request, email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned") ||
                      e.getMessage().contains("No installment plan found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("is required") ||
                      e.getMessage().contains("must be") ||
                      e.getMessage().contains("cannot be")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating installment plan: " + e.getMessage());
        }
    }
}
