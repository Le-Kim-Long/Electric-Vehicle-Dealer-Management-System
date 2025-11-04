package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "DEALER_CAR")
@IdClass(DealerCarId.class)
public class DealerCar {
    @Id
    @Column(name = "DealerID")
    private Integer dealerId;

    @Id
    @Column(name = "CarID")
    private Integer carId;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "DealerPrice", precision = 18, scale = 2)
    private BigDecimal dealerPrice;

    @Column(name = "Status", length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CarID", insertable = false, updatable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DealerID", insertable = false, updatable = false)
    private Dealer dealer;
}