package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "DEALER_CAR")
@IdClass(DealerCarId.class)
public class DealerCar {
    @Id
    @Column(name = "CarID")
    private Integer carId;

    @Id
    @Column(name = "DealerID")
    private Integer dealerId;

    @Column(name = "Quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CarID", insertable = false, updatable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DealerID", insertable = false, updatable = false)
    private Dealer dealer;
}