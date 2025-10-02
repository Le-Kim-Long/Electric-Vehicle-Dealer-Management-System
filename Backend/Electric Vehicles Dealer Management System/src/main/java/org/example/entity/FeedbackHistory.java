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
    private Integer history_id;

    @Column(name = "FeedbackId")
    private Integer feedback_id;   // FK to Feedback

    @Column(name = "EmployeeId", length = 50)
    private String status;

    @Column(name = "ProcessedDate")
    private LocalDateTime processed_date;

    @Column(name = "HandlingNotes", length = 1000)
    private String handling_notes;

    @Column(name = "StatusAfterHandling", length = 50)
    private String status_after_handling;

}
