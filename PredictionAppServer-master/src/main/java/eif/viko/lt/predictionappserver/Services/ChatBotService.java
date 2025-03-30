package eif.viko.lt.predictionappserver.Services;

import eif.viko.lt.predictionappserver.Entities.ChatHistory;
import eif.viko.lt.predictionappserver.Entities.ChatUser;
import eif.viko.lt.predictionappserver.Repositories.ChatHistoryRepository;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatBotService {
    private final DoccatModel model;
    private final ChatHistoryRepository chatHistoryRepository;
    private final Map<String, String> categoryResponses = new HashMap<>();

    public ChatBotService(ChatHistoryRepository chatHistoryRepository,
                          @Value("${chatbot.model.path:src/main/resources/static/trained_models/chatbot-model.bin}") String modelPath) throws IOException {
        this.chatHistoryRepository = chatHistoryRepository;

        try (InputStream modelIn = new FileInputStream(modelPath)) {
            this.model = new DoccatModel(modelIn);
        }

        // Pradiniai atsakymai pagal kategorijas
        initCategoryResponses();
    }

    private void initCategoryResponses() {
        categoryResponses.put("syntax", "Java kintamasis yra atminties vieta, kuri turi vardą ir gali laikyti duomenis. "
                + "Kintamąjį reikia deklaruoti nurodant jo tipą ir pavadinimą, pavyzdžiui: 'int skaicius = 5;'");

        categoryResponses.put("oop", "Paveldėjimas yra vienas iš objektinio programavimo principų, leidžiantis vienai "
                + "klasei (vaiko) perimti kitos klasės (tėvo) metodus ir laukus. Java tai pasiekiama naudojant raktažodį 'extends'.");

        categoryResponses.put("exception", "Išimtis (exception) Java kalboje yra objektas, kuris sukuriamas, kai programos "
                + "vykdymo metu įvyksta klaida. Išimtys gali būti dvi pagrindinės rūšys: 'checked' (tikrinamos) ir 'unchecked' (netikrinamos).");

        categoryResponses.put("spring", "REST API su Spring Boot sukuriamas naudojant anotacijas. "
                + "Svarbiausios anotacijos: @RestController, @RequestMapping, @GetMapping, @PostMapping ir t.t. "
                + "Spring Boot automatiškai konfigūruoja daugumą dalykų, todėl API kūrimas yra paprastesnis.");
    }

    public Map<String, Object> processQuestion(String question, ChatUser user) {
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(question);
        double[] outcomes = categorizer.categorize(tokens);

        String bestCategory = categorizer.getBestCategory(outcomes);
        String response = categoryResponses.getOrDefault(bestCategory,
                "Atsiprašau, bet negaliu atsakyti į šį klausimą. Prašome užduoti klausimą susijusį su Java programavimu.");

        // Išsaugome užklausimą ir atsakymą istorijoje
        if (user != null) {
            ChatHistory history = new ChatHistory();
            history.setMessage("Q: " + question + "\nA: " + response + "\nCategory: " + bestCategory);
            chatHistoryRepository.save(history);
        }

        // Grąžiname kategoriją ir atsakymą
        Map<String, Object> result = new HashMap<>();
        result.put("bestCategory", bestCategory);
        result.put("response", response);

        // Pridedame visas galimas kategorijas ir jų tikimybes
        Map<String, Double> categories = new HashMap<>();
        for (int i = 0; i < outcomes.length; i++) {
            categories.put(categorizer.getCategory(i), outcomes[i]);
        }
        result.put("allCategories", categories);

        return result;
    }

    // Metodas pridėti naują atsakymą į kategoriją
    public void addCategoryResponse(String category, String response) {
        categoryResponses.put(category, response);
    }

    // Gauname visus atsakymus
    public Map<String, String> getAllCategoryResponses() {
        return new HashMap<>(categoryResponses);
    }
}