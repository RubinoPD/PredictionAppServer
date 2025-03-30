package eif.viko.lt.predictionappserver.Controllers;

import eif.viko.lt.predictionappserver.Entities.ChatUser;
import eif.viko.lt.predictionappserver.Entities.GradeHistory;
import eif.viko.lt.predictionappserver.Repositories.GradeHistoryRepository;
import eif.viko.lt.predictionappserver.Entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predict")
public class GradePredictionController {

    private final J48 tree;
    private final Instances data;
    private final GradeHistoryRepository gradeHistoryRepository;

    public GradePredictionController(GradeHistoryRepository gradeHistoryRepository) throws Exception {
        this.gradeHistoryRepository = gradeHistoryRepository;

        // Kelią reikia pritaikyti prie jūsų projekto struktūros
        String modelPath = "src/main/resources/static/trained_models/grade-model.model";
        String dataPath = "src/main/resources/static/stud_grade_training_data.csv";

        // Load the trained model
        this.tree = (J48) SerializationHelper.read(modelPath);

        // Load the dataset to set the class attribute
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(dataPath));
        this.data = loader.getDataSet();
        this.data.setClassIndex(data.numAttributes() - 1);
    }

    @PostMapping("/grade")
    public ResponseEntity<PredictionResponse> predictGrade(@RequestBody Student student) {
        try {
            // Create a new student instance
            Instance studentInstance = new DenseInstance(data.numAttributes());
            studentInstance.setValue(data.attribute("Attendance"), student.getAttendance());
            studentInstance.setValue(data.attribute("Assignments"), student.getAssignments());
            studentInstance.setValue(data.attribute("Midterm"), student.getMidterm());
            studentInstance.setValue(data.attribute("Final"), student.getFinalExam());
            studentInstance.setDataset(data); // Set dataset

            // Predict the final grade
            double prediction = tree.classifyInstance(studentInstance);
            String predictedGrade = data.classAttribute().value((int) prediction);

            // Save prediction history
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof ChatUser) {
                ChatUser currentUser = (ChatUser) authentication.getPrincipal();

                GradeHistory history = new GradeHistory();
                history.setUser(currentUser);
                history.setAttendance(student.getAttendance());
                history.setAssignments(student.getAssignments());
                history.setMidterm(student.getMidterm());
                history.setFinalExam(student.getFinalExam());
                history.setPredictedGrade(predictedGrade);
                // Actual grade will be set later when available

                gradeHistoryRepository.save(history);
            }

            return ResponseEntity.ok(new PredictionResponse(predictedGrade));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new PredictionResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<GradeHistory>> getUserPredictionHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ChatUser) {
            ChatUser currentUser = (ChatUser) authentication.getPrincipal();
            List<GradeHistory> history = gradeHistoryRepository.findByUserOrderByPredictionDateDesc(currentUser);
            return ResponseEntity.ok(history);
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @PutMapping("/history/{id}/actual-grade")
    public ResponseEntity<GradeHistory> updateActualGrade(@PathVariable Long id, @RequestBody ActualGradeDto gradeDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ChatUser) {
            return gradeHistoryRepository.findById(id)
                    .map(history -> {
                        history.setActualGrade(gradeDto.getActualGrade());
                        GradeHistory updated = gradeHistoryRepository.save(history);
                        return ResponseEntity.ok(updated);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/analytics/weak-students")
    public ResponseEntity<List<GradeHistory>> getWeakStudents() {
        // Tik mokytojai ir administratoriai gali peržiūrėti šią informaciją
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ChatUser) {
            ChatUser currentUser = (ChatUser) authentication.getPrincipal();
            if (currentUser.getRole() == Role.TEACHER || currentUser.getRole() == Role.ADMIN) {
                List<GradeHistory> weakStudents = gradeHistoryRepository.findWithLowGrades();
                return ResponseEntity.ok(weakStudents);
            }
        }
        return ResponseEntity.status(403).build();
    }

    @GetMapping("/analytics/accuracy")
    public ResponseEntity<Map<String, Object>> getPredictionAccuracy() {
        // Tik mokytojai ir administratoriai gali peržiūrėti šią informaciją
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ChatUser) {
            ChatUser currentUser = (ChatUser) authentication.getPrincipal();
            if (currentUser.getRole() == Role.TEACHER || currentUser.getRole() == Role.ADMIN) {

                long accuratePredictions = gradeHistoryRepository.countAccuratePredictions();
                long totalPredictionsWithActual = gradeHistoryRepository.countPredictionsWithActualGrades();

                double accuracyPercentage = totalPredictionsWithActual > 0
                        ? (double) accuratePredictions / totalPredictionsWithActual * 100
                        : 0;

                Map<String, Object> result = new HashMap<>();
                result.put("accuratePredictions", accuratePredictions);
                result.put("totalPredictionsWithActual", totalPredictionsWithActual);
                result.put("accuracyPercentage", accuracyPercentage);

                return ResponseEntity.ok(result);
            }
        }
        return ResponseEntity.status(403).build();
    }

    @Getter
    @Setter
    public static class Student {
        private double attendance;
        private double assignments;
        private double midterm;
        private double finalExam;
    }

    @Getter
    @AllArgsConstructor
    public static class PredictionResponse {
        private String predictedGrade;
    }

    @Getter
    @Setter
    public static class ActualGradeDto {
        private String actualGrade;
    }
}