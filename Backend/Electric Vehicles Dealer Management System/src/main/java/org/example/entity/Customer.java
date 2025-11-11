package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "FullName", length = 100)
    private String fullName;

    @Column(name = "PhoneNumber", length = 50)
    private String phoneNumber;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "CreatedDate", length = 255)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Orders> orders;


}
