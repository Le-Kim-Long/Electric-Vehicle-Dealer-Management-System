package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CAR_VARIANT")
public class CarVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VariantId")
    private Integer variantId;

    @Column(name = "VariantName", nullable = false, length = 100)
    private String variantName;

    @Column(name = "ModelId")
    private Integer modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ModelId", insertable = false, updatable = false)
    private CarModel carModel;

    @OneToOne(mappedBy = "carVariant", cascade = CascadeType.ALL)
    private Configuration configuration;

    @Column(name = "Description")
    private String description;

    @OneToMany(mappedBy = "carVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Car> cars;
}