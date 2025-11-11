package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "COLOR")
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ColorID")
    private Integer colorId;

    @Column(name = "ColorName", nullable = false, length = 50)
    private String colorName;

    @OneToMany(mappedBy = "color", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Car> cars;
}
