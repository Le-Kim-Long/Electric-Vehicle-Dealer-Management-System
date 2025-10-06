package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "FeedbackHistory")
public class FeedbackHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HistoryId")
    private Integer historyId;

    @Column(name = "FeedbackId")
    private Integer feedbackId;   // FK to Feedback

    @Column(name = "EmployeeId", length = 50)
    private String employeeId;

    @Column(name = "ProcessedDate")
    private LocalDateTime processedDate;

    @Column(name = "HandlingNotes", length = 1000)
    private String handlingNotes;

    @Column(name = "StatusAfterHandling", length = 50)
    private String statusAfterHandling;

}
