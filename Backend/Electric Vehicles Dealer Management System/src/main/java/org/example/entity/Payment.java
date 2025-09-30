package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer payment_id;

    @Column(name = "order_id")
    private Integer order_id;   // FK to Orders

    @Column(name = "amount")
    private Double amount;

    @Column(name = "payment_date")
    private LocalDateTime payment_date;

    @Column(name = "method", length = 50)
    private String method;


}
