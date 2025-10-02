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
    private Integer order_id;

    @Column(name = "CustomerId")
    private Integer customer_id;   // FK to Customer

    @Column(name = "DealerId")
    private Integer dealer_id;   // FK to Dealer

    @Column(name = "OrderDate")
    private LocalDateTime order_date;

    @Column(name = "SubTotal")
    private int sub_total;

    @Column(name = "DiscountAmount")
    private int discount_amount;

    @Column(name = "TotalAmont")
    private int total_amount;

    @Column(name = "PaymentMethod", length = 50)
    private String payment_method;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "PromotionId")
    private int promotion_id;


}
