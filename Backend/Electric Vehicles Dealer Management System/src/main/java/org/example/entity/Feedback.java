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
    @Column(name = "FeedbackID")
    private Integer feedback_id;

    @Column(name = "OrderID")
    private Integer customer_id;   // FK to Customer

    @Column(name = "Content", length = 1000)
    private String content;

    @Column(name = "SubmittedDate")
    private LocalDateTime submitted_date;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "HandleID")
    private int handle_id;

    @Column(name = "HandledDate")
    private LocalDateTime handled_date;



}
