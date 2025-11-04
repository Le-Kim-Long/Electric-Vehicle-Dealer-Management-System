package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountResponse {
    private Integer userId;
    private String username;
    private String email;
    private String password; // Thêm trường password
    private String phoneNumber;
    private String status;
    private LocalDateTime createdDate;
    private String roleName;
    private String dealerName;
    private Integer dealerId;
}
