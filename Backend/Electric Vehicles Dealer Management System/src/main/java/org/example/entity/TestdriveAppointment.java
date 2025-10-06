package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
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
    @Column(name = "AppointmentId")
    private Integer appointmentId;

    @Column(name = "CustomerId")
    private Integer customerId;   // FK to Customer

    @Column(name = "CartId")
    private Integer cartId;        // FK to Car

    @Column(name = "DealerId")
    private Integer dealerId;     // FK to Dealer

    @Column(name = "AppointmentDate")
    private LocalDateTime appointmentDate;

    @Column(name = "AppointmentTime")
    private Time appointmentTime;

    @Column(name = "Notes", length = 50)
    private String notes;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

}
