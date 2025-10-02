package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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
    private Integer installment_id;

    @Column(name = "OrderID")
    private Integer order_id;   // FK to Orders

    @Column(name = "TermCount")
    private double term_count;

    @Column(name = "AmountPerTerm")
    private double amount_per_term;

    @Column(name = "InterestRate", length = 50)
    private double interest_rate;

    @Column(name = "Note", length = 200)
    private String note;

}
