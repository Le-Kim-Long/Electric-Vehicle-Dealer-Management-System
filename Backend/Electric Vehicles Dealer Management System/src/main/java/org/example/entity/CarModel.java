package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CAR_MODEL")
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ModelId")
    private Integer modelId;

    @Column(name = "ModelName", nullable = false, length = 100)
    private String modelName;

    @Column(name = "Segment", length = 100)
    private String segment;

    @OneToMany(mappedBy = "carModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarVariant> carVariants;
}