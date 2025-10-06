package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ORDERS")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Integer orderId;

    @Column(name = "CustomerId")
    private Integer customerId;   // FK to Customer

    @Column(name = "DealerId")
    private Integer dealerId;   // FK to Dealer

    @Column(name = "OrderDate")
    private LocalDateTime orderDate;

    @Column(name = "SubTotal")
    private int subTotal;

    @Column(name = "DiscountAmount")
    private int discountAmount;

    @Column(name = "TotalAmont")
    private int totalAmont;

    @Column(name = "PaymentMethod", length = 50)
    private String paymentMethod;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "PromotionId")
    private int promotionId;


}
