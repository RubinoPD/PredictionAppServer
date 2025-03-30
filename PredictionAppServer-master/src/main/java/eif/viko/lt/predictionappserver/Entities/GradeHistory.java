package eif.viko.lt.predictionappserver.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ChatUser user;

    private double attendance;
    private double assignments;
    private double midterm;
    private double finalExam;
    private String predictedGrade;
    private String actualGrade;
    private LocalDateTime predictionDate;

    @PrePersist
    protected void onCreate() {
        this.predictionDate = LocalDateTime.now();
    }
}
