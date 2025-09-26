package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class LoginResponse {
    private String token;
    private String role;
    private String fullName;




    // getters and setters
}
