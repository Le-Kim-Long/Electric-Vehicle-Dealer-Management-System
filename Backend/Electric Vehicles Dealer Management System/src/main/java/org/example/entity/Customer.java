package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private Integer customer_id;

    @Column(name = "FullName", length = 100)
    private String full_name;

    @Column(name = "PhoneNumber", length = 50)
    private String phone_number;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "CreatedDate", length = 255)
    private LocalDateTime created_date;


}
