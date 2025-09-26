package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NGUOIDUNG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNguoiDung")
    private Integer maNguoiDung;


    @Column(name = "HoTen")
    private String hoTen;

    @Column(name = "Email")
    private String email;

    @Column(name = "MatKhau")
    private String matKhau;

    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaVaiTro")
    private VaiTro vaiTro;

    @Column(name = "TrangThai")
    private String trangThai;
}