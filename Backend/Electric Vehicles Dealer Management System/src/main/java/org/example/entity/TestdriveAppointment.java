package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TESTDRIVE_APPOINTMENT")
public class TestdriveAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer appointment_id;

    @Column(name = "customer_id")
    private Integer customer_id;   // FK to Customer

    @Column(name = "car_id")
    private Integer car_id;        // FK to Car

    @Column(name = "appointment_date")
    private LocalDateTime appointment_date;

    @Column(name = "status", length = 50)
    private String status;


}
