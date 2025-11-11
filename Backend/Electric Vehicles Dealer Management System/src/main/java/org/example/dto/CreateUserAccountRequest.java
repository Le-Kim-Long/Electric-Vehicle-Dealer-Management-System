package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserAccountRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{9,15}$", message = "Phone number must be 9-15 digits and can contain +, -, or spaces")
    private String phoneNumber;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(DealerStaff|EVMStaff|DealerManager)$",
             message = "Role must be one of: DealerStaff, EVMStaff, DealerManager")
    private String roleName;

    // Dealer name chỉ cần thiết khi role là DealerStaff hoặc DealerManager
    private String dealerName;
}
