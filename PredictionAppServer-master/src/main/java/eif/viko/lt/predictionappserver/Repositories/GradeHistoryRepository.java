package eif.viko.lt.predictionappserver.Repositories;

import eif.viko.lt.predictionappserver.Entities.ChatUser;
import eif.viko.lt.predictionappserver.Entities.GradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeHistoryRepository extends JpaRepository<GradeHistory, Long> {

    List<GradeHistory> findByUserOrderByPredictionDateDesc(ChatUser user);

    @Query("SELECT g FROM GradeHistory g WHERE g.predictedGrade IN ('D', 'F')")
    List<GradeHistory> findWithLowGrades();

    @Query("SELECT COUNT(g) FROM GradeHistory g WHERE g.predictedGrade = g.actualGrade AND g.actualGrade IS NOT NULL")
    long countAccuratePredictions();

    @Query("SELECT COUNT(g) FROM GradeHistory g WHERE g.actualGrade IS NOT NULL")
    long countPredictionsWithActualGrades();

}
