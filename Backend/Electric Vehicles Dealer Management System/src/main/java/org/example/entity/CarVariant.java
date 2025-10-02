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
    private Integer variant_id;

    @Column(name = "VariantName", nullable = false, length = 100)
    private String variant_name;

    @Column(name = "ModelId")
    private Integer model_id;   // FK to CarModel

    @Column(name = "Description")
    private Double description;

}
