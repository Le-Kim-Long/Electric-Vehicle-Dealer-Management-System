package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

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


}
