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
    @Column(name = "DealerID")
    private Integer dealer_id;

    @Column(name = "DealerName", nullable = false, length = 100)
    private String dealer_name;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "PhoneNumber", length = 50)
    private String phone_number;

    @Column(name = "Email", length = 100)
    private String email;


}
