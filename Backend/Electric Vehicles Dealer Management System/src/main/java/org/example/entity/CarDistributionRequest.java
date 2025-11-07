package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CAR_DISTRIBUTION_REQUEST")
public class CarDistributionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestID")
    private Integer requestId;

    @ManyToOne
    @JoinColumn(name = "DealerID", nullable = false)
    private Dealer dealer;

    @ManyToOne
    @JoinColumn(name = "CarID", nullable = false)
    private Car car;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "RequestDate", nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();

    @Column(name = "Status", nullable = false, length = 50)
    private String status = "PENDING";
    // Chờ duyệt | Đã duyệt | Từ chối | Đang giao | Đã giao

    @Column(name = "ApprovedDate")
    private LocalDateTime approvedDate;

    @Column(name = "ExpectedDeliveryDate")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "ActualDeliveryDate")
    private LocalDateTime actualDeliveryDate;
}
