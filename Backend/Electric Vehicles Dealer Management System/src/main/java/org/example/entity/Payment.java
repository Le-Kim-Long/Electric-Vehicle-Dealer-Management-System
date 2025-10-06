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
    @Column(name = "PaymentID")
    private Integer paymentID;

    @Column(name = "OrderID")
    private Integer orderID;   // FK to Orders

    @Column(name = "Amount")
    private Double amount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "Method", length = 50)
    private String method;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "Note", length = 255)
    private String note;


}
