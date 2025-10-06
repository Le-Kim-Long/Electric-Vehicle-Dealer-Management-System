package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PROMOTION")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionID")
    private Integer promotionID;

    @Column(name = "PromotionName", length = 100)
    private String promotionName;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "Value")
    private String value;

    @Column(name = "Type", length = 50)
    private String type;

    @Column(name = "Scope", length = 20)
    private String scope;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;


}
