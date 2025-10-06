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
    private Integer userID;

    @Column(name = "FullName", nullable = false, length = 100, unique = true)
    private String fullName;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleID")
    private Role roleID;   // FK to Role

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "PhoneNumber", length = 15)
    private int phoneNumber;

    @Column(name = "DealerID")
    private String dealerID; // FK to Dealer

    @Column(name = "Status")
    private String status;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

}
