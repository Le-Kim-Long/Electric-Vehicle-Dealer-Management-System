package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Promotion_Dealer")
public class PromotionDealer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "promotion_id")
    private Integer promotion_id;   // FK to Promotion

    @Column(name = "dealer_id")
    private Integer dealer_id;      // FK to Dealer


}
