package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Installment")
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InstallmentId")
    private Integer installmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Orders order;

    @Column(name = "PrincipalAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal principalAmount;   // tiền gốc

    @Column(name = "TermCount", nullable = false)
    private Integer termCount;            // số kỳ trả góp (12, 24, ...)

    @Column(name = "InterestRate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;     // lãi suất (%/năm)

    @Column(name = "TotalInterest", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalInterest;    // tổng tiền lãi phải trả

    @Column(name = "TotalPay", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPay;         // tổng số tiền phải trả

    @Column(name = "AmountPerTerm", nullable = false, precision = 18, scale = 2)
    private BigDecimal amountPerTerm;    // tiền trả mỗi kỳ

    @Column(name = "Note", length = 200)
    private String note;
}
