package eif.viko.lt.predictionappserver.Controllers;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.CSVLoader;

import java.io.File;


@RestController
@RequestMapping("/api/predict")
public class GradePredictionController {

    private final J48 tree;
    private final Instances data;

    public GradePredictionController() throws Exception {
        // Load the trained model
        this.tree = (J48) SerializationHelper.read("C:\\Users\\mgzeg\\IdeaProjects\\PredictionAppServer\\src\\main\\resources\\static\\trained_models\\grade-model.model");

        // Load the dataset to set the class attribute
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("C:\\Users\\mgzeg\\IdeaProjects\\PredictionAppServer\\src\\main\\resources\\static\\stud_grade_training_data.csv"));
        this.data = loader.getDataSet();
        this.data.setClassIndex(data.numAttributes() - 1);
    }

    @PostMapping("/grade")
    public ResponseEntity<String> predictGrade(@RequestBody Student student) {
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
            return ResponseEntity.ok(predictedGrade);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error predicting grade: " + e.getMessage());
        }
    }


    @Getter
    @Setter
    public static class Student {
        private double attendance;
        private double assignments;
        private double midterm;
        private double finalExam;
    }

}
