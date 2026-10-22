package com.example.ai_finance_manager.ai_finance.ai;

import com.example.ai_finance_manager.ai_finance.expense.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Slf4j
@Service
public class GeminiCategoryService implements CategoryService {

    private static final List<String> CATEGORIES =
            List.of("Food", "Travel", "Bills", "Shopping", "Health", "Entertainment", "Other");

    private static final int MAX_NOTE_CHARS = 80;
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+");
    private static final Pattern LONG_DIGITS = Pattern.compile("\\d{4,}");
    private static final Pattern NON_LETTERS = Pattern.compile("[^A-Za-z]");

    private static final Map<String, Set<String>> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("Food", Set.of("swiggy", "zomato", "blinkit", "zepto", "bigbasket", "restaurant",
                "cafe", "coffee", "tea", "lunch", "dinner", "breakfast", "snacks", "pizza", "burger",
                "biryani", "grocery", "groceries", "food", "dominos", "starbucks"));
        KEYWORDS.put("Travel", Set.of("uber", "ola", "rapido", "metro", "bus", "train", "irctc", "flight",
                "cab", "taxi", "auto", "petrol", "diesel", "fuel", "toll", "parking", "redbus", "ticket"));
        KEYWORDS.put("Bills", Set.of("electricity", "water", "rent", "recharge", "jio", "airtel", "vi",
                "wifi", "broadband", "gas", "emi", "insurance", "bill", "bills", "maintenance", "dth"));
        KEYWORDS.put("Shopping", Set.of("amazon", "flipkart", "myntra", "ajio", "meesho", "nykaa", "mall",
                "clothes", "shoes", "shopping", "shirt", "jeans", "dress", "gadget"));
        KEYWORDS.put("Health", Set.of("pharmacy", "medicine", "medicines", "doctor", "hospital", "clinic",
                "gym", "apollo", "medplus", "lab", "dentist", "checkup", "health"));
        KEYWORDS.put("Entertainment", Set.of("netflix", "spotify", "movie", "movies", "cinema", "bookmyshow",
                "prime", "hotstar", "youtube", "game", "games", "concert", "pvr", "inox"));
    }

    private final GeminiClient geminiClient;
    private final Timer latencyTimer;
    private final Counter fallbackCounter;
    private final MeterRegistry meterRegistry;
    private final LruCache<String, String> cache = new LruCache<>(1000);

    public GeminiCategoryService(GeminiClient geminiClient, MeterRegistry meterRegistry) {
        this.geminiClient = geminiClient;
        this.meterRegistry = meterRegistry;
        this.latencyTimer = Timer.builder("ai.category.latency")
                .description("Latency of Gemini category suggestions")
                .publishPercentileHistogram()
                .register(meterRegistry);
        this.fallbackCounter = Counter.builder("ai.category.fallback")
                .description("Category suggestions answered by keyword rules instead of Gemini")
                .register(meterRegistry);
    }


    @Override
    public String suggest(String note) {
        if (note == null || note.isBlank()) {
            return DEFAULT_CATEGORY;
        }
        String key = note.trim().toLowerCase(Locale.ROOT);

        String cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        String safeNote = sanitize(key);
        if (geminiClient.isConfigured() && !safeNote.isBlank()) {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                String answer = validate(geminiClient.generate(buildPrompt(safeNote), 8));
                if (answer != null) {
                    cache.put(key, answer);
                    return answer;
                }
            } catch (Exception ex) {
                log.warn("Gemini category call failed, using keyword rules: {}",
                        ex.getClass().getSimpleName());
            } finally {
                sample.stop(latencyTimer);
            }
        }
        fallbackCounter.increment();
        return keywordCategory(key);
    }

    private String buildPrompt(String safeNote) {
        return "Classify the expense note into exactly one of: "
                + String.join(",", CATEGORIES) + ". Reply with the word only.\nNote: " + safeNote;
    }

    private String sanitize(String note) {
        String cleaned = EMAIL.matcher(note).replaceAll(" ");
        cleaned = LONG_DIGITS.matcher(cleaned).replaceAll(" ").trim();
        return cleaned.length() > MAX_NOTE_CHARS ? cleaned.substring(0, MAX_NOTE_CHARS) : cleaned;
    }

    private String validate(String answer) {
        String letters = NON_LETTERS.matcher(answer).replaceAll("");
        for (String category : CATEGORIES) {
            if (category.equalsIgnoreCase(letters)) {
                return category;
            }
        }
        return null;
    }

    private String keywordCategory(String lowerNote) {
        String[] tokens = lowerNote.split("[^a-z]+");
        for (Map.Entry<String, Set<String>> entry : KEYWORDS.entrySet()) {
            for (String token : tokens) {
                if (!token.isEmpty() && entry.getValue().contains(token)) {
                    return entry.getKey();
                }
            }
        }
        return DEFAULT_CATEGORY;
    }
}
