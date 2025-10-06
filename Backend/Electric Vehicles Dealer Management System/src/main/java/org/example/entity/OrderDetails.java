package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ORDER_DETAILS")
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId;

    @Column(name = "order_id")
    private Integer orderId;   // FK to Orders

    @Column(name = "car_id")
    private Integer carId;     // FK to Car

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Double price;


}
