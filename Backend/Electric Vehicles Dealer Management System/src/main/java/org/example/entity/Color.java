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
    @Column(name = "ColorID")
    private Integer colorID;

    @Column(name = "ColorName", nullable = false, length = 50)
    private String colorName;

//    @Column(name = "hex_code", length = 7)
//    private String hex_code;


}
