package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "COLOR")
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_id")
    private Integer color_id;

    @Column(name = "color_name", nullable = false, length = 50)
    private String color_name;

    @Column(name = "hex_code", length = 7)
    private String hex_code;


}
