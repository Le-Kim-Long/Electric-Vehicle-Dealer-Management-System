package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.CreatePaymentRequest;
import org.example.dto.CreatePaymentResponse;
import org.example.dto.PaymentResponse;
import org.example.dto.UpdatePaymentMethodRequest;
import org.example.dto.UpdatePaymentMethodResponse;
import org.example.dto.UpdatePaymentStatusRequest;
import org.example.dto.UpdatePaymentStatusResponse;
import org.example.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealer/payments")
@Tag(name = "Dealer Payment Management", description = "APIs for managing payments - DealerManager and DealerStaff only")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("")
    @Operation(
        summary = "Create payment for order",
        description = "Creates a new payment record for an order with status 'Chờ xử lý'. " +
                     "Only dealer manager and dealer staff can create payments. " +
                     "Payment can only be created for orders with status 'Chưa thanh toán'. " +
                     "Payment logic differs based on order's payment method: " +
                     "- 'Trả thẳng' (Cash/Credit Card/Bank Transfer): Only 1 payment allowed with amount = order.totalAmount " +
                     "- 'Trả góp' (Installment): Multiple payments allowed (up to termCount) with amount = installment.amountPerTerm " +
                     "Order must belong to the current user's dealer and must have totalAmount > 0. " +
                     "For installment orders, an installment plan must exist first. " +
                     "Supported payment methods: Tiền mặt, Chuyển khoản. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data, validation failed, invalid payment method, order total amount is zero, payment limit reached, missing installment plan, or order status is not 'Chưa thanh toán'"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can create payments"),
        @ApiResponse(responseCode = "404", description = "Order not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "409", description = "Conflict - Payment already exists for 'Trả thẳng' order or maximum payments reached for installment order"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createPayment(
            @Parameter(description = "Payment creation details. Amount will be determined automatically based on order's payment method: totalAmount for 'Trả thẳng', amountPerTerm for 'Trả góp'. Order must have status 'Chưa thanh toán'", required = true)
            @Valid @RequestBody CreatePaymentRequest request) {
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

            // Tạo payment
            CreatePaymentResponse response = paymentService.createPayment(request, email);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("can only have 1 payment") ||
                      e.getMessage().contains("Maximum number of payments") ||
                      e.getMessage().contains("already reached")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            } else if (e.getMessage().contains("Invalid payment method") ||
                      e.getMessage().contains("is required") ||
                      e.getMessage().contains("must be") ||
                      e.getMessage().contains("must have an installment plan") ||
                      e.getMessage().contains("can only be created for orders with status")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while creating payment: " + e.getMessage());
        }
    }

    @PutMapping("/{paymentId}/method")
    @Operation(
        summary = "Update payment method",
        description = "Updates the payment method for an existing payment and automatically resets status to 'Chờ xử lý'. " +
                     "Only dealer manager and dealer staff can update payment methods. " +
                     "Payment must belong to the current user's dealer (through the associated order). " +
                     "Supported payment methods: Tiền mặt, Chuyển khoản. " +
                     "Status will be automatically set to 'Chờ xử lý' after update. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment method updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data, validation failed, or invalid payment method"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can update payment methods or payment doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Payment not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updatePaymentMethod(
            @Parameter(description = "Payment ID to update method for", required = true)
            @PathVariable Integer paymentId,
            @Parameter(description = "Payment method update details", required = true)
            @Valid @RequestBody UpdatePaymentMethodRequest request) {
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

            // Cập nhật payment method
            UpdatePaymentMethodResponse response = paymentService.updatePaymentMethod(paymentId, request, email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Invalid payment method") ||
                      e.getMessage().contains("is required") ||
                      e.getMessage().contains("must be")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating payment method: " + e.getMessage());
        }
    }

    @PutMapping("/{paymentId}/status")
    @Operation(
        summary = "Update payment status with automatic order status update",
        description = "Updates the status of an existing payment with intelligent order status automation. " +
                     "Only dealer manager and dealer staff can update payment status. " +
                     "Payment must belong to the current user's dealer (through the associated order). " +
                     "Valid status values: 'Chờ xử lý', 'Hoàn thành', 'Thất bại'. " +
                     "AUTOMATIC ORDER STATUS UPDATE LOGIC: " +
                     "- For 'Trả thẳng' orders: When payment status is set to 'Hoàn thành', order status is automatically updated to 'Đã thanh toán'. " +
                     "- For 'Trả góp' orders: When payment status is set to 'Hoàn thành', system checks if total completed payment amounts >= order total amount. If yes, order status is automatically updated to 'Đã thanh toán'. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment status updated successfully with order status automatically updated if applicable"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data, validation failed, or invalid status value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can update payment status or payment doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Payment not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updatePaymentStatus(
            @Parameter(description = "Payment ID to update status for", required = true)
            @PathVariable Integer paymentId,
            @Parameter(description = "Payment status update details", required = true)
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
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

            // Cập nhật payment status
            UpdatePaymentStatusResponse response = paymentService.updatePaymentStatus(paymentId, request, email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Invalid status value") ||
                      e.getMessage().contains("is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating payment status: " + e.getMessage());
        }
    }

    @DeleteMapping("/{paymentId}")
    @Operation(
        summary = "Delete payment",
        description = "Deletes an existing payment record. " +
                     "Only dealer manager and dealer staff can delete payments. " +
                     "Payment must belong to the current user's dealer (through the associated order). " +
                     "Once deleted, the payment record will be permanently removed from the system. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can delete payments or payment doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Payment not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deletePayment(
            @Parameter(description = "Payment ID to delete", required = true)
            @PathVariable Integer paymentId) {
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

            // Xóa payment
            paymentService.deletePayment(paymentId, email);

            return ResponseEntity.ok("Payment deleted successfully");

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while deleting payment: " + e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    @Operation(
        summary = "Get all payments by order ID",
        description = "Retrieves all payments associated with a specific order ID. " +
                     "Only dealer manager and dealer staff can access payment information. " +
                     "Order must belong to the current user's dealer. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can access payments or order doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Order not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getPaymentsByOrderId(
            @Parameter(description = "Order ID to retrieve payments for", required = true)
            @PathVariable Integer orderId) {
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

            // Lấy danh sách payments theo orderId
            List<PaymentResponse> payments = paymentService.getPaymentsByOrderId(orderId, email);

            return ResponseEntity.ok(payments);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while retrieving payments: " + e.getMessage());
        }
    }
}
