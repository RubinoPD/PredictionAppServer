package eif.viko.lt.predictionappserver.TrainModels;

import opennlp.tools.doccat.*;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class TrainChatBot {
    public static void main(String[] args) throws Exception {

        // Data for training
        var filePath = "C:\\Users\\mgzeg\\IdeaProjects\\PredictionAppServer\\src\\main\\resources\\static\\chat_training_data.txt";

        InputStream inputFile = new FileInputStream(filePath);

        ObjectStream<String> lineStream = new PlainTextByLineStream(()-> inputFile, "UTF-8");
        ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

        // Train
        DoccatFactory factory = new DoccatFactory();
        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, 100);
        params.put(TrainingParameters.CUTOFF_PARAM, 1);

        DoccatModel model = DocumentCategorizerME.train("lt", sampleStream, params, factory);

        // Trained model output path
        var outputPath = "C:\\Users\\mgzeg\\IdeaProjects\\PredictionAppServer\\src\\main\\resources\\static\\trained_models";

        try(OutputStream out = new FileOutputStream(outputPath+"\\chatbot-model.bin")) {
            model.serialize(out);
        }
        System.out.println("Model training complete!!!");

    }
}
