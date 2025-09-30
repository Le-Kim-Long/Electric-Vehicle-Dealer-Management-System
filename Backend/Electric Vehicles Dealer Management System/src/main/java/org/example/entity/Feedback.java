package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedback_id;

    @Column(name = "customer_id")
    private Integer customer_id;   // FK to Customer

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "created_at")
    private LocalDateTime created_at;


}
