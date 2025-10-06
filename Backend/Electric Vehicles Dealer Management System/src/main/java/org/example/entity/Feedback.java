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
    private Integer feedbackID;

    @Column(name = "OrderID")
//    private Integer customer_id;   // FK to Customer
    private Integer orderID;

    @Column(name = "Content", length = 1000)
    private String content;

    @Column(name = "SubmittedDate")
    private LocalDateTime submittedDate;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "HandleID")
    private int handleID;

    @Column(name = "HandledDate")
    private LocalDateTime handledDate;



}
