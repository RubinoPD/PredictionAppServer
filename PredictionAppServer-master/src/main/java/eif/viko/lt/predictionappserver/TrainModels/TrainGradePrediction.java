package eif.viko.lt.predictionappserver.TrainModels;

import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.CSVLoader;

import java.io.File;


public class TrainGradePrediction {
    public static void main(String[] args) {

        var path = "C:\\Users\\mgzeg\\IdeaProjects\\PredictionAppServer\\src\\main\\resources\\static\\stud_grade_training_data.csv";
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(path));
            Instances data = loader.getDataSet();
            // Paskutinis parametras
            data.setClassIndex(data.numAttributes() - 1);

            // Naudojam sprendinių medi klasifikavimui
            J48 tree = new J48();
            tree.buildClassifier(data);

            var outputPath = "C:\\Users\\mgzeg\\IdeaProjects\\PredictionAppServer\\src\\main\\resources\\static\\trained_models\\grade-model.model";
            SerializationHelper.write(outputPath, tree);
            System.out.println("Prediction model trained!!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
