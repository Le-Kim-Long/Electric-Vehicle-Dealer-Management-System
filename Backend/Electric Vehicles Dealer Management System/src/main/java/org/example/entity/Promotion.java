package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PROMOTION")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionId")
    private Integer promotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DealerId", nullable = false)
    private Dealer dealer;

    @Column(name = "PromotionName", length = 100)
    private String promotionName;

    @Column(name = "Description", length = 250)
    private String description;

    @Column(name = "Value")
    private BigDecimal value;

    @Column(name = "Type", length = 50)
    private String type;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Orders> orders;
}
