package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Installment")
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InstallmentID")
    private Integer installmentID;

    @Column(name = "OrderID")
    private Integer orderID;   // FK to Orders

    @Column(name = "TermCount")
    private double termCount;

    @Column(name = "AmountPerTerm")
    private double amountPerTerm;

    @Column(name = "InterestRate", length = 50)
    private double interestRate;

    @Column(name = "Note", length = 200)
    private String note;

}
