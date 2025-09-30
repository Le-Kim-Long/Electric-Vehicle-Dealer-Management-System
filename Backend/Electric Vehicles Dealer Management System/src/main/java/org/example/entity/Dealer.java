package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "DEALER")
public class Dealer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dealer_id")
    private Integer dealer_id;

    @Column(name = "dealer_name", nullable = false, length = 100)
    private String dealer_name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "phone_number", length = 50)
    private String phone_number;

    @Column(name = "email", length = 100)
    private String email;


}
