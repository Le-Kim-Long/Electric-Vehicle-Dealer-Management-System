package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CAR")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CarID")
    private Integer carId;

    @Column(name = "VariantId")
    private Integer variantId;

    @Column(name = "ColorID")
    private Integer colorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VariantId", insertable = false, updatable = false)
    private CarVariant carVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ColorID", insertable = false, updatable = false)
    private Color color;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DealerCar> dealerCars;

    @Column(name = "ProductionYear")
    private Integer productionYear;

    @Column(name = "Price")
    private Long price;

    @Column(name = "ImagePath")
    private String imagePath;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarDistributionRequest> carDistributionRequests;
}
