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
public class TestDriveAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppointmentId")
    private Integer appointment_id;

    @Column(name = "CustomerId")
    private Integer customer_id;   // FK to Customer

    @Column(name = "CarId")
    private Integer car_id;        // FK to Car

    @Column(name = "DealerId")
    private Integer dealer_id;     // FK to Dealer

    @Column(name = "AppointmentDate")
    private LocalDateTime appointment_date;

    @Column(name = "AppointmentTime")
    private Time appointment_time;

    @Column(name = "Notes", length = 50)
    private String notes;

    @Column(name = "CreatedDate")
    private LocalDateTime created_date;

}
