package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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
    @Column(name = "PaymentId")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Orders order;

    @Column(name = "Amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "Method", length = 50, nullable = false)
    private String method;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "Note", length = 250)
    private String note;
}
