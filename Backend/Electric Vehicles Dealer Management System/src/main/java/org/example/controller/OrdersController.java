package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.*;
import org.example.service.OrdersService;
import org.example.service.InstallmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealer/orders")
@Tag(name = "Dealer Orders Management", description = "APIs for managing orders of dealer - DealerManager and DealerStaff only")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private InstallmentService installmentService;

    @GetMapping("")
    @Operation(
        summary = "Get all orders of current dealer",
        description = "Returns a list of all orders belonging to the dealer that the current logged-in user (DealerManager or DealerStaff) is assigned to. " +
                     "Each order includes complete customer information, order details, payment information, and promotion details if applicable. " +
                     "Orders are sorted by order date in descending order (newest first). " +
                     "Only accessible by DealerManager and DealerStaff roles. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all orders of the dealer"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "User not found or not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getDealerOrders() {
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

            // Lấy orders của dealer
            List<DealerOrderResponse> orders = ordersService.getOrdersByCurrentUserDealer(email);

            return ResponseEntity.ok(orders);

        } catch (RuntimeException e) {
            // Xử lý lỗi access denied hoặc user not found
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching dealer orders: " + e.getMessage());
        }
    }

    @PostMapping("/draft")
    @Operation(
        summary = "Create a draft order",
        description = "Creates a new draft order with status 'Chưa xác nhận'. " +
                     "Only dealer manager and dealer staff can create draft orders. " +
                     "The dealer ID is automatically set to the dealer that the current user belongs to. " +
                     "Requires customer ID as input. " +
                     "Initial order will have zero amounts and no payment method."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Draft order created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can create draft orders"),
        @ApiResponse(responseCode = "404", description = "Customer not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createDraftOrder(@RequestBody CreateDraftOrderRequest request) {
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

            // Tạo draft order
            CreateDraftOrderResponse response = ordersService.createDraftOrder(request, email);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while creating draft order: " + e.getMessage());
        }
    }

    @PostMapping("/details")
    @Operation(
        summary = "Create order detail",
        description = "Creates a new order detail for an existing order. " +
                     "Only dealer manager and dealer staff can create order details. " +
                     "The car is automatically found by model name, variant name, and color name. " +
                     "Unit price is automatically set from dealer price. " +
                     "Inventory quantity is automatically reduced after creation. " +
                     "Order must belong to the current user's dealer."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order detail created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can create order details"),
        @ApiResponse(responseCode = "404", description = "Order not found, car not found in dealer inventory, or insufficient inventory"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createOrderDetail(@RequestBody CreateOrderDetailRequest request) {
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

            // Tạo order detail
            CreateOrderDetailResponse response = ordersService.createOrderDetail(request, email);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned") ||
                      e.getMessage().contains("Insufficient inventory")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while creating order detail: " + e.getMessage());
        }
    }

    @PutMapping("/details/{orderDetailId}")
    @Operation(
        summary = "Update order detail",
        description = "Updates an order detail quantity with automatic inventory calculation and order subTotal update. " +
                     "Only dealer manager and dealer staff can update order details. " +
                     "Inventory will be automatically adjusted based on quantity changes. " +
                     "If quantity increases: additional inventory will be reserved from dealer stock. " +
                     "If quantity decreases: excess inventory will be returned to dealer stock. " +
                     "Order subTotal will be automatically recalculated after update. " +
                     "Order detail must belong to the current user's dealer."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order detail updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can update order details"),
        @ApiResponse(responseCode = "404", description = "Order detail not found or dealer car record not found"),
        @ApiResponse(responseCode = "409", description = "Insufficient inventory for quantity increase"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateOrderDetail(
            @Parameter(description = "Order detail ID to update", required = true)
            @PathVariable Integer orderDetailId,
            @Parameter(description = "Order detail update information", required = true)
            @RequestBody UpdateOrderDetailRequest request) {
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

            // Cập nhật order detail
            UpdateOrderDetailResponse response = ordersService.updateOrderDetail(orderDetailId, request, email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Insufficient inventory")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating order detail: " + e.getMessage());
        }
    }

    @DeleteMapping("/details/{orderDetailId}")
    @Operation(
        summary = "Delete order detail",
        description = "Deletes an order detail and returns the inventory quantity back to dealer. " +
                     "Only dealer manager and dealer staff can delete order details. " +
                     "The inventory quantity will be automatically restored to dealer inventory. " +
                     "Order subTotal will be automatically recalculated after deletion. " +
                     "Order detail must belong to the current user's dealer."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order detail deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can delete order details"),
        @ApiResponse(responseCode = "404", description = "Order detail not found or dealer car record not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteOrderDetail(
            @Parameter(description = "Order detail ID to delete", required = true)
            @PathVariable Integer orderDetailId) {
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

            // Xóa order detail
            ordersService.deleteOrderDetail(orderDetailId, email);

            return ResponseEntity.ok("Order detail deleted successfully. Inventory quantity restored and order subTotal updated.");

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while deleting order detail: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/promotion")
    @Operation(
        summary = "Update order promotion",
        description = "Updates promotion for an order with automatic discount and total amount calculation. " +
                     "Only dealer manager and dealer staff can update order promotion. " +
                     "Promotion must belong to the current user's dealer and be active. " +
                     "For VND type: totalAmount = subTotal - discountAmount (where discountAmount = promotion value). " +
                     "For % type: totalAmount = subTotal - (subTotal * promotion value percentage). " +
                     "Order must have subTotal > 0 to apply promotion. " +
                     "If promotionId is null or not provided, the promotion will be removed from the order."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order promotion updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data, inactive promotion, or invalid date range"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can update order promotion"),
        @ApiResponse(responseCode = "404", description = "Order not found, promotion not found, or promotion doesn't belong to dealer"),
        @ApiResponse(responseCode = "409", description = "Conflict - Negative total amount after applying promotion"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateOrderPromotion(
            @Parameter(description = "Order ID to update promotion", required = true)
            @PathVariable Integer orderId,
            @Parameter(description = "Promotion ID to apply (optional - null to remove promotion)", required = false)
            @RequestBody UpdateOrderPromotionRequest request) {
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

            // Cập nhật promotion cho order - truyền orderId từ path parameter
            UpdateOrderPromotionResponse response = ordersService.updateOrderPromotion(orderId, request, email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned") ||
                      e.getMessage().contains("does not belong")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("not active") ||
                      e.getMessage().contains("not valid for current date") ||
                      e.getMessage().contains("must be greater than 0") ||
                      e.getMessage().contains("Invalid promotion type")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } else if (e.getMessage().contains("cannot be negative")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating order promotion: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/payment-method")
    @Operation(
        summary = "Update order payment method",
        description = "Updates the payment method for an order. " +
                     "Only dealer manager and dealer staff can update order payment method. " +
                     "Order must belong to the current user's dealer. " +
                     "Supported payment methods: Credit Card, Cash, Bank Transfer, Installment, " +
                     "Thẻ tín dụng, Tiền mặt, Chuyển khoản, Trả góp. " +
                     "Payment method validation is case-insensitive."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order payment method updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or unsupported payment method"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can update order payment method"),
        @ApiResponse(responseCode = "404", description = "Order not found or order doesn't belong to dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateOrderPaymentMethod(
            @Parameter(description = "Order ID to update payment method", required = true)
            @PathVariable Integer orderId,
            @Parameter(description = "Payment method information", required = true)
            @RequestBody UpdateOrderPaymentMethodRequest request) {
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

            // Cập nhật payment method cho order
            UpdateOrderPaymentMethodResponse response = ordersService.updateOrderPaymentMethod(orderId, request, email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned") ||
                      e.getMessage().contains("does not belong")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Invalid payment method") ||
                      e.getMessage().contains("is required") ||
                      e.getMessage().contains("cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating order payment method: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/status")
    @Operation(
        summary = "Update order status",
        description = "Updates the status of an order. " +
                     "Only dealer manager and dealer staff can update order status. " +
                     "Order must belong to the current user's dealer. " +
                     "Supported statuses: Chưa xác nhận, Đã xác nhận, Đang xử lý, Đã giao hàng, Đã hủy. " +
                     "Status validation is case-insensitive."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or unsupported status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can update order status"),
        @ApiResponse(responseCode = "404", description = "Order not found or order doesn't belong to dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateOrderStatus(
            @Parameter(description = "Order ID to update status", required = true)
            @PathVariable Integer orderId,
            @Parameter(description = "Status information", required = true)
            @RequestBody UpdateOrderStatusRequest request) {
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

            // Cập nhật status cho order
            UpdateOrderStatusResponse response = ordersService.updateOrderStatus(orderId, request, email);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned") ||
                      e.getMessage().contains("does not belong")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("Invalid order status") ||
                      e.getMessage().contains("is required") ||
                      e.getMessage().contains("cannot be null")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating order status: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by order ID",
        description = "Returns complete order information including customer details, dealer information, and all order details for a specific order ID. " +
                     "Order must belong to the dealer that the current logged-in user (DealerManager or DealerStaff) is assigned to. " +
                     "Response includes order information, customer details, dealer details, and list of order details with car specifications. " +
                     "Only accessible by DealerManager and DealerStaff roles. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order details"),
        @ApiResponse(responseCode = "400", description = "Bad request - Order ID is required"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can access order details or order doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Order not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getOrderById(
            @Parameter(description = "Order ID to retrieve", required = true)
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

            // Lấy thông tin order theo ID
            DealerOrderResponse orderResponse = ordersService.getOrderById(orderId, email);

            return ResponseEntity.ok(orderResponse);

        } catch (RuntimeException e) {
            // Xử lý lỗi access denied hoặc order not found
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching order details: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/details")
    @Operation(
        summary = "Get order details by order ID",
        description = "Returns a list of all order details for a specific order ID. " +
                     "Each order detail includes complete car information including model name, variant name, color name, quantity, unit price, and final price. " +
                     "Order must belong to the dealer that the current logged-in user (DealerManager or DealerStaff) is assigned to. " +
                     "Only accessible by DealerManager and DealerStaff roles. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order details"),
        @ApiResponse(responseCode = "400", description = "Bad request - Order ID is required"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can access order details or order doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Order not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getOrderDetailsByOrderId(
            @Parameter(description = "Order ID to retrieve details for", required = true)
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

            // Lấy danh sách order details theo order ID
            List<OrderDetailResponse> orderDetails = ordersService.getOrderDetailsByOrderId(orderId, email);

            return ResponseEntity.ok(orderDetails);

        } catch (RuntimeException e) {
            // Xử lý lỗi access denied hoặc order not found
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching order details: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/payment-method")
    @Operation(
        summary = "Get order payment method by order ID",
        description = "Returns the payment method information for a specific order ID. " +
                     "Response includes order ID, payment method, current order status, and success message. " +
                     "Order must belong to the dealer that the current logged-in user (DealerManager or DealerStaff) is assigned to. " +
                     "Only accessible by DealerManager and DealerStaff roles. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order payment method"),
        @ApiResponse(responseCode = "400", description = "Bad request - Order ID is required"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can access order payment method or order doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Order not found or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getOrderPaymentMethod(
            @Parameter(description = "Order ID to retrieve payment method for", required = true)
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

            // Lấy payment method của order theo ID
            OrderPaymentMethodResponse paymentMethodResponse = ordersService.getOrderPaymentMethod(orderId, email);

            return ResponseEntity.ok(paymentMethodResponse);

        } catch (RuntimeException e) {
            // Xử lý lỗi access denied hoặc order not found
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching order payment method: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/installment")
    @Operation(
        summary = "Get installment information by order ID",
        description = "Returns complete installment plan information for a specific order ID. " +
                     "Response includes installment ID, principal amount, term count, interest rate, total interest, " +
                     "total pay amount, amount per term, and additional notes if any. " +
                     "Order must belong to the dealer that the current logged-in user (DealerManager or DealerStaff) is assigned to. " +
                     "Order must have an existing installment plan. " +
                     "Only accessible by DealerManager and DealerStaff roles. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved installment information"),
        @ApiResponse(responseCode = "400", description = "Bad request - Order ID is required"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can access installment information or order doesn't belong to dealer"),
        @ApiResponse(responseCode = "404", description = "Order not found, no installment plan found for order, or user not assigned to any dealer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getOrderInstallment(
            @Parameter(description = "Order ID to retrieve installment information for", required = true)
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

            // Lấy thông tin installment của order theo ID
            GetInstallmentByOrderResponse installmentResponse = installmentService.getInstallmentByOrderId(orderId, email);

            return ResponseEntity.ok(installmentResponse);

        } catch (RuntimeException e) {
            // Xử lý lỗi access denied hoặc installment not found
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") ||
                      e.getMessage().contains("not assigned") ||
                      e.getMessage().contains("No installment plan found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching installment information: " + e.getMessage());
        }
    }
}
