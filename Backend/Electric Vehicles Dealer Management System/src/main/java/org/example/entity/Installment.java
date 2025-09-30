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
    @Column(name = "installment_id")
    private Integer installment_id;

    @Column(name = "order_id")
    private Integer order_id;   // FK to Orders

    @Column(name = "amount")
    private Double amount;

    @Column(name = "due_date")
    private LocalDate due_date;

    @Column(name = "status", length = 50)
    private String status;


}
