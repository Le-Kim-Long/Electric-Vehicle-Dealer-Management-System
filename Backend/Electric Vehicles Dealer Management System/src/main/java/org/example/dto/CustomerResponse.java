package org.example.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponse {
    private Integer customerId;
    private String fullName;
    private String phoneNumber;
    private String email;
}

