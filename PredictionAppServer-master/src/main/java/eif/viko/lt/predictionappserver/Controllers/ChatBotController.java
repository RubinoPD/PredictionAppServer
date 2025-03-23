package eif.viko.lt.predictionappserver.Controllers;


import lombok.AllArgsConstructor;
import lombok.Getter;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


@RestController
@RequestMapping("/chatbot")
public class ChatBotController {
    private DoccatModel model;

    public ChatBotController() throws IOException {
        InputStream customModel = new FileInputStream("C:\\Users\\mgzeg\\IdeaProjects\\PredictionAppServer\\src\\main\\resources\\static\\trained_models\\chatbot-model.bin");
        model = new DoccatModel(customModel);
    }

    // DTO
    @Getter
    @AllArgsConstructor
    public static class CategorizationResponse {
        private String bestCategory;
        private String[] allCategories;
    }

    @GetMapping("/ask")
    public ResponseEntity<CategorizationResponse> ask(@RequestParam String question) {
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(question);
        double[] outcomes = categorizer.categorize(tokens);

        // Get all categories
        String[] categories = new String[outcomes.length];
        for (int i = 0; i < outcomes.length; i++) {
            categories[i] = categorizer.getCategory(i);
        }

        // Wrap in a DTO
        CategorizationResponse response = new CategorizationResponse(categorizer.getBestCategory(outcomes), categories);

        return ResponseEntity.ok(response);
    }

}



