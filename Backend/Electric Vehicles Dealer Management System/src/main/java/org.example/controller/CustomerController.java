package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.CreateCustomerRequest;
import org.example.dto.CreateCustomerResponse;
import org.example.dto.CustomerResponse;
import org.example.dto.UpdateCustomerRequest;
import org.example.dto.UpdateCustomerResponse;
import org.example.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "APIs for managing customers and their orders")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("")
    @Operation(
        summary = "Create a new customer",
        description = "Creates a new customer in the system. " +
                     "Returns the customer ID upon successful creation. " +
                     "Email and phone number must be unique. " +
                     "Full name should contain only letters and spaces. " +
                     "Phone number should be 10 or 11 digits."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Customer created successfully, returns customer ID"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or validation failed"),
        @ApiResponse(responseCode = "409", description = "Conflict - Email or phone number already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createCustomer(
            @Parameter(description = "Customer information to create", required = true)
            @RequestBody CreateCustomerRequest request) {
        try {
            CreateCustomerResponse response = customerService.createCustomer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Xử lý lỗi validation hoặc conflict
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while creating customer: " + e.getMessage());
        }
    }

    @PutMapping("/{customerId}")
    @Operation(
        summary = "Update customer information",
        description = "Updates customer information by ID. " +
                     "Only dealer manager and dealer staff can update customer information. " +
                     "At least one field (fullName, email, phoneNumber) must be provided. " +
                     "Email and phone number must be unique if provided. " +
                     "Full name should contain only letters and spaces. " +
                     "Phone number should be 10 or 11 digits. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data or validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can update customer information"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "409", description = "Conflict - Email or phone number already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateCustomer(
            @Parameter(description = "Customer ID to update", required = true)
            @PathVariable Integer customerId,
            @Parameter(description = "Customer information to update", required = true)
            @RequestBody UpdateCustomerRequest request) {
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

            // Cập nhật customer với authentication
            UpdateCustomerResponse response = customerService.updateCustomer(customerId, request, email);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các loại lỗi khác nhau
            if (e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("not assigned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating customer: " + e.getMessage());
        }
    }

    @GetMapping("/{customerId}")
    @Operation(
        summary = "Get customer information by ID",
        description = "Retrieves customer information including orders by customer ID. " +
                     "Only dealer manager and dealer staff can access customer information. " +
                     "Returns customer details with associated orders if any exist. " +
                     "Requires JWT token in Authorization header."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only DealerManager and DealerStaff can access customer information"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCustomerById(
            @Parameter(description = "Customer ID to retrieve", required = true)
            @PathVariable Integer customerId) {
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

            // Lấy customer information với authentication
            CustomerResponse response = customerService.getCustomerById(customerId, email);
            return ResponseEntity.ok(response);

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
                .body("An error occurred while retrieving customer: " + e.getMessage());
        }
    }
}
