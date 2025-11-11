package org.example.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCustomerRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
}