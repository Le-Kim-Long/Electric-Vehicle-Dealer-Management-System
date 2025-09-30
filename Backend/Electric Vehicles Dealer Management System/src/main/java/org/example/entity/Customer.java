package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customer_id;

    @Column(name = "first_name", length = 100)
    private String first_name;

    @Column(name = "last_name", length = 100)
    private String last_name;

    @Column(name = "phone_number", length = 50)
    private String phone_number;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 255)
    private String address;


}
