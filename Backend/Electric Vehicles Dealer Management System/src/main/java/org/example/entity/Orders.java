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
    @Column(name = "order_id")
    private Integer order_id;

    @Column(name = "customer_id")
    private Integer customer_id;   // FK to Customer

    @Column(name = "order_date")
    private LocalDateTime order_date;

    @Column(name = "status", length = 50)
    private String status;


}
