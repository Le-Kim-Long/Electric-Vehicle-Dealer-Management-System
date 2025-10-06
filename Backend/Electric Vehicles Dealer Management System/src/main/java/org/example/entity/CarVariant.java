package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private Integer modelId;   // FK to CarModel

    @Column(name = "Description")
    private String description;

}
