package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "VAITRO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VaiTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaVaiTro")
    private Integer id;

    @Column(name = "TenVaiTro")
    private String tenVaiTro;

    @Column(name = "MoTa")
    private String moTa;
}