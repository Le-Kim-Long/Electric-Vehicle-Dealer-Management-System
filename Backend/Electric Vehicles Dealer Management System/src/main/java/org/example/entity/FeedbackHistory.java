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
    @Column(name = "history_id")
    private Integer history_id;

    @Column(name = "feedback_id")
    private Integer feedback_id;   // FK to Feedback

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;


}
