package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USER_ACCOUNT")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userId;

    @Column(name = "FullName", nullable = false, length = 100)
    private String username;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleID")
    private Role roleId;

    @Column(name = "Email", length = 100, unique = true)
    private String email;

    @Column(name = "PhoneNumber", length = 15, unique = true)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DealerID")
    private Dealer dealer;

    @Column(name = "Status")
    private String status;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;
}
