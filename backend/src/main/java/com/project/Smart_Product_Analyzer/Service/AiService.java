package com.project.Smart_Product_Analyzer.Service;

import com.project.Smart_Product_Analyzer.Config.PromptLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;

    @Autowired
    public AiService(ChatClient.Builder chatClientBuilder, PromptLoader promptLoader) {
        this.chatClient = chatClientBuilder.build();
        this.promptLoader = promptLoader;
    }

    /**
     * Orchestrates the analysis of all reviews by splitting them into batches
     * and processing them in parallel.
     */
    public String getProductAnalysisResponse(String reviewsText) {
        // Parse the single string back into a list of reviews for batching
        // (Assuming the input is newline separated as per previous logic)
        List<String> allReviews = List.of(reviewsText.split("\n"));

        if (allReviews.isEmpty())
            return "No reviews to analyze.";

        // Split into batches of 5
        List<List<String>> batches = chunkList(allReviews, 5);
        System.out.println("Split " + allReviews.size() + " reviews into " + batches.size() + " batches.");

        // Fire off async requests
        List<CompletableFuture<String>> futures = batches.stream()
                .map(this::analyzeBatch)
                .toList();

        // Wait for all to complete and join results
        List<String> batchSummaries = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // Final step: Aggregate the batch summaries into one final analysis
        // If there's only one batch, return it directly
        if (batchSummaries.size() == 1) {
            return batchSummaries.get(0);
        }

        // Otherwise, ask AI to combine them
        return aggregateSummaries(batchSummaries);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> analyzeBatch(List<String> reviewsBatch) {
        try {
            String combinedReviews = String.join("\n", reviewsBatch);
            String prompt = promptLoader.loadPromptFile("productAnalyzer.txt");
            String fullPrompt = prompt.replace("{reviews}", combinedReviews);

            String response = chatClient.prompt()
                    .user(fullPrompt)
                    .call()
                    .content();

            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            System.err.println("Error in batch analysis: " + e.getMessage());
            return CompletableFuture.completedFuture("Error analyzing batch: " + e.getMessage());
        }
    }

    private String aggregateSummaries(List<String> summaries) {
        try {
            String combinedSummaries = String.join("\n\n=== NEXT BATCH ===\n\n", summaries);
            // We use a simpler prompt for aggregation
            String aggregationPrompt = "Here are multiple partial analyses of a product based on different review batches. "
                    +
                    "Please combine them into one consistent, final analysis following the same PROS, CONS, VERDICT, RATING format:\n\n"
                    +
                    combinedSummaries;

            return chatClient.prompt()
                    .user(aggregationPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            System.err.println("Error aggregating summaries: " + e.getMessage());
            return summaries.get(0); // Fallback to first batch
        }
    }

    // Helper to chunk the list
    private <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return chunks;
    }

    public String getSearchQueryResponse(String query) {
        try {
            String prompt = promptLoader.loadPromptFile("searchQueryPrompt.txt").replace("{query}", query);
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new RuntimeException("File Handling Error..");
        }
    }

    public List<String> generateSearchUrls(String description) {
        try {
            String promptTemplate = promptLoader.loadPromptFile("searchUrlPrompt.txt");
            String prompt = promptTemplate.replace("{description}", description);

            String response = chatClient.prompt()
                    .user(prompt)
                    .options(OpenAiChatOptions.builder()
                            .withModel("accounts/fireworks/models/gpt-oss-120b")
                            .build())
                    .call()
                    .content();

            System.out.println("Generated Search URLs: " + response);

            if (response == null || response.trim().isEmpty()) {
                return new ArrayList<>();
            }

            // Split by newline and filter empty lines
            return List.of(response.split("\n")).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && s.startsWith("http"))
                    .toList();

        } catch (Exception e) {
            System.err.println("Error generating search URLs: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String extractProductKeyword(String productTitle) {
        try {
            String promptTemplate = promptLoader.loadPromptFile("keywordExtractionPrompt.txt");
            String prompt = promptTemplate.replace("{productTitle}", productTitle);

            String response = chatClient.prompt()
                    .user(prompt)
                    .options(OpenAiChatOptions.builder()
                            .withModel("accounts/fireworks/models/gpt-oss-20b") // User requested specific model
                            .build())
                    .call()
                    .content();

            if (response != null) {
                return response.trim();
            }
            return "";
        } catch (Exception e) {
            System.err.println("Error extracting product keyword: " + e.getMessage());
            return productTitle; // Fallback to full title
        }
    }
}