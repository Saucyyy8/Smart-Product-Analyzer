package com.project.Smart_Product_Analyzer.Service;

import com.project.Smart_Product_Analyzer.Exception.InvalidUrlException;
import com.project.Smart_Product_Analyzer.Exception.ProductNotFound;
import com.project.Smart_Product_Analyzer.Exception.ScrapingException;
import com.project.Smart_Product_Analyzer.Model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.project.Smart_Product_Analyzer.Model.ProductAnalysisRequest;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.project.Smart_Product_Analyzer.repository.ProductHistoryRepository;
import com.project.Smart_Product_Analyzer.entity.ProductHistory;
import com.project.Smart_Product_Analyzer.entity.User;
import com.project.Smart_Product_Analyzer.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    private final AmazonScraperService scraperService;
    private final AmazonSearchPageScraper searchScraperService;
    private final AiService aiService;
    private final Executor taskExecutor;
    private final ProductHistoryRepository productHistoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProductService(AmazonScraperService scraperService,
            AmazonSearchPageScraper searchScraperService,
            AiService aiService,
            @Qualifier("taskExecutor") Executor taskExecutor,
            ProductHistoryRepository productHistoryRepository,
            UserRepository userRepository) {
        this.scraperService = scraperService;
        this.searchScraperService = searchScraperService;
        this.aiService = aiService;
        this.taskExecutor = taskExecutor;
        this.productHistoryRepository = productHistoryRepository;
        this.userRepository = userRepository;
    }

    @Cacheable("Product")
    public List<Product> analyzeProduct(String productDescription) {
        return analyzeProductInternal(productDescription, null);
    }

    private List<Product> analyzeProductInternal(String productDescription, String username) {
        log.info("Starting optimized product analysis for description: {}", productDescription);

        // Step 0: Check Cache (History)
        List<Product> cachedProduct = checkHistoryCache(productDescription);
        if (cachedProduct != null && !cachedProduct.isEmpty()) {
            log.info("Found cached product analysis for: {}", productDescription);
            return cachedProduct;
        }

        try {
            // Reverted: Use AI for URL Generation as per user request
            List<String> searchUrls = aiService.generateSearchUrls(productDescription);

            if (searchUrls.isEmpty()) {
                throw new ProductNotFound("AI failed to generate any valid search URLs.");
            }

            log.info("AI generated {} search URLs", searchUrls.size());

            // Shallow Scrape & Filter First
            List<Product> shallowProducts = new ArrayList<>();
            boolean searchSuccessful = false;

            for (String searchUrl : searchUrls) {
                try {
                    log.info("Attempting to shallow scrape search URL: {}", searchUrl);
                    shallowProducts = searchScraperService.scrapeSearchPage(searchUrl);

                    if (!shallowProducts.isEmpty()) {
                        searchSuccessful = true;
                        log.info("Successfully found {} shallow products from URL: {}", shallowProducts.size(),
                                searchUrl);
                        break; // Stop after first successful scraping
                    }
                } catch (Exception e) {
                    log.warn("Failed to shallow scrape search URL: {}. Error: {}", searchUrl, e.getMessage());
                }
            }

            if (!searchSuccessful || shallowProducts.isEmpty()) {
                throw new ProductNotFound("No products found on search page from any generated URL.");
            }

            // Filter: Rating >= 4.0 & Score
            List<Product> candidates = shallowProducts.stream()
                    .filter(p -> p.getRating() != null && p.getRating() >= 4.0)
                    .sorted((p1, p2) -> Double.compare(p2.getRating(), p1.getRating())) // Sort by rating desc
                    .limit(5) // Only take top 5 survivors
                    .toList();

            log.info("Filtered down to {} high-quality candidates (>= 4.0 stars) for deep analysis.",
                    candidates.size());

            if (candidates.isEmpty()) {
                // Fallback: If no 4-star products, take top 3 of whatever we have
                log.warn("No high-rated products found. Falling back to top 3 raw results.");
                candidates = shallowProducts.stream().limit(3).toList();
            }

            // Extract Links for Deep Processing
            List<String> candidateLinks = candidates.stream().map(Product::getUrl).toList();

            // Step 3: Deep Process Winners (Concurrent)
            List<Product> bestProducts = findBestProducts(candidateLinks);

            if (bestProducts.isEmpty()) {
                throw new ProductNotFound("No suitable products found after analysis.");
            }

            Product bestProduct = bestProducts.get(0);

            saveHistory(productDescription, bestProduct, username);

            return bestProducts;


        } catch (Exception e) {
            log.error("Error analyzing product: ", e);
            throw new ScrapingException("Failed to analyze product: " + e.getMessage(), e);
        }
    }

    @Cacheable("Product")
    public List<Product> analyzeLink(String link) {
        log.info("Starting product analysis for URL: {}", link);

        // Step 0: Check Cache
        List<Product> cachedProduct = checkHistoryCache(link);
        if (cachedProduct != null && !cachedProduct.isEmpty()) {
            log.info("Found cached product analysis for link: {}", link);
            return cachedProduct;
        }

        validateAmazonUrl(link);

        try {
            // Scrape the product
            // Scrape the main product
            Product mainProduct = scraperService.scrapeAmazonOnUrl(link);
            analyzeProductReviews(mainProduct);

            // Generate similar products
            List<Product> similarProducts = new ArrayList<>();
            try {
                String keyword = aiService.extractProductKeyword(mainProduct.getName());
                if (!keyword.isEmpty()) {
                    log.info("Extracted keyword for similar search: {}", keyword);
                    List<String> searchUrls = aiService.generateSearchUrls(keyword);

                    // We have Search URLs (e.g. amazon.in/s?k=...), we need to extract Product URLs
                    // from them first
                    List<String> productLinksForSimilar = new ArrayList<>();

                    for (String sUrl : searchUrls) {
                        try {
                            // Scrape the search page to get product links
                            List<String> links = searchScraperService.scrapeAmazonOnUrl(sUrl);
                            if (!links.isEmpty()) {
                                productLinksForSimilar.addAll(links);
                            }
                            // If we have enough links, stop
                            if (productLinksForSimilar.size() >= 5)
                                break;
                        } catch (Exception e) {
                            log.warn("Failed to scrape search page for similar products: {}", sUrl);
                        }
                    }

                    // Scrape top 3 similar products from the found links
                    if (!productLinksForSimilar.isEmpty()) {
                        List<String> limitedLinks = productLinksForSimilar.stream().distinct().limit(3).toList();
                        log.info("Found {} product links for similar items, analyzing top 3...", limitedLinks.size());
                        List<Product> foundSimilar = findBestProducts(limitedLinks);
                        similarProducts.addAll(foundSimilar);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch similar products: " + e.getMessage());
            }

            log.info("Successfully analyzed product from link: {}", mainProduct.getName());

            // Save history
            // Save history
            saveHistory(link, mainProduct, null); // analyzeLink is cached/sync so usually has context, but can pass
                                                  // null to auto-detect if needed, OR overload.
            // Wait, analyzeLink is called by Controller which has context. But wait,
            // analyzeLinkStream calls THIS analyzeLink? No, it calls extract... logic.
            // analyzeLink is a public method called by Controller (Sync).
            // Let's check analyzeLink signature. It doesn't take username.
            // I should overload or just pass null to let it detect from Context.

            // However, analyzeProductStream calls analyzeProduct (Async) -> saveHistory
            // (Async).
            // So analyzeProduct needs to take username too? Or I should pass username to
            // saveHistory inside analyzeProduct?
            // "analyzeProduct" is public. I can't easily change signature without breaking
            // controller.
            // Better to OVERLOAD analyzeProduct or make a private helper.

            // Actually, for now, let's just make saveHistory take (query, product,
            // username).
            // In analyzeLink (Sync), we pass null (it will find context).
            // In analyzeLinkStream (Async), we pass username.

            // Oops, I'm editing multiple places.
            // Let's fix analyzeProduct (Sync, called by Async Stream) first.
            // If analyzeProduct is called from Stream, it has NO Context.
            // So analyzeProduct MUST accept username if we want it to work in Stream.
            // But changing public API is annoying.
            // Let's make an overloaded private method `analyzeProduct(desc, username)`?
            // Or just pass the username to `analyzeProduct` if I change the signature.

            // Simpler: Just update `analyzeLinkStream` to call `saveHistory` MANUALLY
            // instead of inside `analyzeLink`?
            // `analyzeLinkStream` calls `scraperService.scrape...` directly! It does NOT
            // call `analyzeLink`.
            // So `analyzeLink` is ONLY called by Sync Controller.
            // So passing `null` here is fine (it picks up Context).

            List<Product> allProducts = new ArrayList<>();
            allProducts.add(mainProduct);
            // Filter out duplicates (if main product appears in search results)
            for (Product p : similarProducts) {
                // Simple duplicate check by name similarity or just strict string check
                if (!p.getName().equals(mainProduct.getName())) {
                    allProducts.add(p);
                }
            }

            return allProducts;

        } catch (Exception e) {
            log.error("Error analyzing product from link: ", e);
            throw new ScrapingException("Failed to analyze product from link: " + e.getMessage(), e);
        }
    }

    private void validateAmazonUrl(String link) {
        if (link == null || link.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be null or empty");
        }

        if (!link.toLowerCase().contains("amazon")) {
            throw new InvalidUrlException("Only Amazon URLs are supported");
        }
    }

    private List<Product> findBestProducts(List<String> productLinks) {
        // Limit to top 10 products
        List<String> limitedLinks = productLinks.stream()
                .limit(10)
                .toList();

        log.info("Scraping {} products concurrently...", limitedLinks.size());

        List<CompletableFuture<Product>> futures = limitedLinks.stream()
                .map(link -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Product product = scraperService.scrapeAmazonOnUrl(link);

                        // Check if rating is missing and default it
                        if (product.getRating() == null) {
                            product.setRating(0.0);
                        }

                        // Concurrent AI analysis of reviews
                        analyzeProductReviews(product);

                        return product;
                    } catch (Exception e) {
                        log.warn("Failed to scrape/analyze product at {}: {}", link, e.getMessage());
                        return null; // Return null on failure
                    }
                }, taskExecutor))
                .toList();

        // Wait for all to complete
        List<Product> products = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // Filter out failed scrapes
                .collect(Collectors.toList());

        log.info("Successfully scraped and analyzed {} products", products.size());

        List<Product> validProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.isValid() && product.getRating() != 0.0) {
                validProducts.add(product);
                log.debug("Added valid product: {} with rating: {}", product.getName(), product.getRating());
            } else {
                log.debug("Product failed validation: {} (name: {}, rating: {})",
                        product.getName(), product.getName() != null, product.getRating());
            }
        }

        log.info("Found {} valid products out of {} attempted", validProducts.size(), limitedLinks.size());

        if (validProducts.isEmpty()) {
            return new ArrayList<>();
        }

        // Sort by rating
        validProducts.sort((p1, p2) -> {
            Double rating1 = p1.getRating();
            Double rating2 = p2.getRating();
            if (rating1 == null)
                rating1 = 0.0;
            if (rating2 == null)
                rating2 = 0.0;
            return Double.compare(rating2, rating1); // Descending order
        });

        // Return top 4
        return validProducts.stream().limit(5).collect(Collectors.toList());
    }

    private void analyzeProductReviews(Product product) {
        try {
            List<String> reviews = product.getPros();
            log.info("Analyzing product: {} with {} reviews", product.getName(),
                    reviews != null ? reviews.size() : 0);

            if (reviews == null || reviews.isEmpty() || reviews.get(0).equals("No reviews found")) {
                log.warn("No reviews found for product: {}, setting default analysis", product.getName());
                setDefaultAnalysis(product);
                return;
            }

            String reviewsText = String.join("\n", reviews);
            log.info("Sending {} reviews to AI for analysis...", reviews.size());

            // This is now internally concurrent if there are many reviews!
            String aiResponse = aiService.getProductAnalysisResponse(reviewsText);

            log.info("Received AI analysis response for product: {}", product.getName());
            parseAnalysisResponse(product, aiResponse);
            log.info("Successfully analyzed product: {} with rating: {}", product.getName(), product.getRating());
        } catch (Exception e) {
            log.error("Error analyzing product reviews for {}: ", product.getName(), e);
            setDefaultAnalysis(product);
        }
    }

    private void parseAnalysisResponse(Product product, String aiResponse) {
        log.info("Parsing AI response: {}", aiResponse);

        // Clear existing raw reviews so they don't show up if parsing fails partially
        product.setPros(new ArrayList<>());
        product.setCons(new ArrayList<>());
        product.setVerdict("Analysis failed to parse."); // Default message

        // Flexible regex to handle: "**PROS**", "PROS:", "PROS", etc.
        // Flexible regex to handle: "**PROS**", "PROS:", "PROS", etc., but MUST be at
        // start of line/string
        // We use (?:^|\n) to ensure we are matching a header line, not a word inside a
        // sentence.
        String prosPattern = "(?:^|\\n)\\s*(?:\\*\\*|#)*\\s*PROS(?:\\*\\*|#|:|\\s)*([\\s\\S]*?)(?=(?:^|\\n)\\s*(?:\\*\\*|#)*\\s*(?:CONS|VERDICT|RATING)|$)";
        String consPattern = "(?:^|\\n)\\s*(?:\\*\\*|#)*\\s*CONS(?:\\*\\*|#|:|\\s)*([\\s\\S]*?)(?=(?:^|\\n)\\s*(?:\\*\\*|#)*\\s*(?:VERDICT|RATING)|$)";
        String verdictPattern = "(?:^|\\n)\\s*(?:\\*\\*|#)*\\s*VERDICT(?:\\*\\*|#|:|\\s)*([\\s\\S]*?)(?=(?:^|\\n)\\s*(?:\\*\\*|#)*\\s*RATING|$)";
        String ratingPattern = "(?:^|\\n)\\s*(?:\\*\\*|#)*\\s*RATING(?:\\*\\*|#|:|\\s)*([\\s\\S]*?)(?=\\s*$)";

        java.util.regex.Pattern prosRegex = java.util.regex.Pattern.compile(prosPattern,
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher prosMatcher = prosRegex.matcher(aiResponse);
        if (prosMatcher.find()) {
            String prosText = prosMatcher.group(1).trim();
            List<String> pros = parseListItems(prosText);
            product.setPros(pros);
        }

        java.util.regex.Pattern consRegex = java.util.regex.Pattern.compile(consPattern,
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher consMatcher = consRegex.matcher(aiResponse);
        if (consMatcher.find()) {
            String consText = consMatcher.group(1).trim();
            List<String> cons = parseListItems(consText);
            product.setCons(cons);
        }

        java.util.regex.Pattern verdictRegex = java.util.regex.Pattern.compile(verdictPattern,
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher verdictMatcher = verdictRegex.matcher(aiResponse);
        if (verdictMatcher.find()) {
            String verdict = verdictMatcher.group(1).trim();
            product.setVerdict(verdict);
        }

        java.util.regex.Pattern ratingRegex = java.util.regex.Pattern.compile(ratingPattern,
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher ratingMatcher = ratingRegex.matcher(aiResponse);
        if (ratingMatcher.find()) {
            try {
                String ratingStr = ratingMatcher.group(1).trim();
                // Handle negative numbers or weird formatting if necessary, but usually rating
                // is positive
                // clean up any non-numeric chars except dot and minus (if we want to capture
                // negative)
                // But normally rating is 0-10.
                ratingStr = ratingStr.replaceAll("[^0-9.]", "");
                if (!ratingStr.isEmpty()) {
                    Double rating = Double.parseDouble(ratingStr);
                    product.setRating(rating);
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse rating", e);
                // Keep default or set to 0.0
            }
        }
    }

    private List<String> parseListItems(String text) {
        List<String> items = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("-") || trimmedLine.startsWith("*") || trimmedLine.startsWith("•")) {
                trimmedLine = trimmedLine.substring(1).trim();
            } else if (trimmedLine.matches("^\\d+\\..*")) {
                trimmedLine = trimmedLine.replaceFirst("^\\d+\\.", "").trim();
            }

            // Clean up artifacts like [ ... ]
            trimmedLine = trimmedLine.replaceAll("^\\[|]$", "").trim();

            if (!trimmedLine.isEmpty()) {
                items.add(trimmedLine);
            }
        }

        return items.isEmpty() ? List.of("No items found") : items;
    }

    private void setDefaultAnalysis(Product product) {
        product.setPros(List.of("No reviews available"));
        product.setCons(List.of("No reviews available"));
        product.setVerdict("Unable to provide analysis due to lack of reviews or AI error.");
        product.setRating(0.0);
    }

    private void saveHistory(String query, Product product, String username) {
        try {
            // If username not provided (e.g. from async thread), try matching context
            if (username == null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && !"anonymousUser".equals(authentication.getPrincipal())) {
                    username = authentication.getName();
                }
            }

            if (username != null) {
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    ProductHistory history = ProductHistory.builder()
                            .user(user)
                            .searchQuery(query)
                            .productName(product.getName())
                            .productRating(product.getRating())
                            .summary(product.getVerdict())
                            .imageUrl(product.getImageUrl())
                            .productUrl(product.getUrl())
                            .build();
                    productHistoryRepository.save(history);
                    log.info("Saved product analysis history for user: {}", username);
                } else {
                    log.warn("User  not found for history saving: {}", username);
                }
            } else {
                log.debug("No user context for history saving.");
            }
        } catch (Exception e) {
            log.error("Failed to save product history: ", e);
        }
    }

    private List<Product> checkHistoryCache(String query) {
        try {
            ProductHistory history = productHistoryRepository
                    .findTopBySearchQueryOrProductNameOrderByCreatedAtDesc(query, query);

            if (history != null) {
                log.info("Cache hit for query: {}", query);
                Product p = Product.builder()
                        .name(history.getProductName())
                        .rating(history.getProductRating())
                        .verdict(history.getSummary())
                        .imageUrl(history.getImageUrl())
                        .url(history.getProductUrl())
                        .pros(List.of("Retrieved from history"))
                        .cons(List.of("See verdict for details"))
                        .build();
                return List.of(p);
            }
        } catch (Exception e) {
            log.warn("Cache check failed: {}", e.getMessage());
        }
        return null;
    }

    public void analyzeProductStream(ProductAnalysisRequest request, SseEmitter emitter) {
        // Capture authentication from the main thread
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            username = authentication.getName();
        }
        final String finalUsername = username;

        taskExecutor.execute(() -> {
            try {
                // Step 0: Check Cache (Global for Stream)
                List<Product> cachedProduct = checkHistoryCache(request.getInput());
                if (cachedProduct != null && !cachedProduct.isEmpty()) {
                    log.info("Streaming cached result for: {}", request.getInput());
                    for (Product p : cachedProduct) {
                        // Default recommended flag for cached items if not set
                        if (p.isRecommended() == false && cachedProduct.indexOf(p) == 0)
                            p.setRecommended(true);
                        emitter.send(p);
                    }
                    emitter.complete();
                    return;
                }

                if (request.isUrl()) {
                    analyzeLinkStream(request.getInput(), emitter, finalUsername);
                } else {
                    List<Product> products = analyzeProductInternal(request.getInput(), finalUsername);
                    // Should theoretically pass username to analyzeProduct too if we wanted history
                    // there to work in async,
                    // but analyzeProduct is currently synchronous so it's fine if called directly.
                    // WAIT: analyzeProduct calls saveHistory which calls SecurityContext.
                    // Since we are inside taskExecutor here, analyzeProduct WILL fail to save
                    // history.
                    // We should overload analyzeProduct or just manually save history here.

                    // Actually, let's keep it simple. History saving for DESCRIPTION analysis via
                    // stream might fail for now.
                    // I'll focus on LINK analysis which is the main slow part.

                    for (int i = 0; i < products.size(); i++) {
                        Product p = products.get(i);
                        if (i == 0) {
                            p.setRecommended(true); // First product is always the main recommendation
                        } else {
                            p.setRecommended(false);
                        }
                        emitter.send(p);
                    }
                    emitter.complete();
                }
            } catch (Exception e) {
                log.error("Error in streaming analysis", e);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("Error completing emitter with error", ex);
                }
            }
        });
    }

    private void analyzeLinkStream(String link, SseEmitter emitter, String username) {
        log.info("Starting streaming analysis for URL: {}", link);
        validateAmazonUrl(link);

        try {
            // 1. Scrape & Analyze MAIN PRODUCT
            Product mainProduct = scraperService.scrapeAmazonOnUrl(link);
            analyzeProductReviews(mainProduct);
            mainProduct.setRecommended(true); // Mark as recommended/main

            // EMIT MAIN PRODUCT IMMEDIATELY
            log.info("Emitting main product: {}", mainProduct.getName());
            emitter.send(mainProduct);

            // Save History Manually using the passed username
            saveHistory(link, mainProduct, username);

            // 2. Background: Find Similar Products
            String keyword = aiService.extractProductKeyword(mainProduct.getName());

            // Fallback if keyword extraction fails or returns empty
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("Keyword extraction failed. Using truncated product name as fallback.");
                // Use first 5 words of title as fallback
                String[] words = mainProduct.getName().split("\\s+");
                keyword = "";
                for (int i = 0; i < Math.min(words.length, 5); i++) {
                    keyword += words[i] + " ";
                }
                keyword = keyword.trim();
            }

            if (!keyword.isEmpty()) {
                log.info("Using keyword for similar search: {}", keyword);

                // Reverted: Use AI for URL generation
                List<String> searchUrls = aiService.generateSearchUrls(keyword);

                List<Product> allShallowSimilar = new ArrayList<>();

                for (String sUrl : searchUrls) {
                    try {
                        List<Product> shallow = searchScraperService.scrapeSearchPage(sUrl);
                        if (!shallow.isEmpty()) {
                            allShallowSimilar.addAll(shallow);
                        }
                        if (allShallowSimilar.size() >= 15)
                            break;
                    } catch (Exception e) {
                        log.warn("Failed to shallow scrape similar products from {}", sUrl);
                    }
                }

                if (!allShallowSimilar.isEmpty()) {
                    // Filter Similar Products: Rating >= 4.0 & Score
                    List<Product> similarCandidates = allShallowSimilar.stream()
                            .filter(p -> p.getRating() != null && p.getRating() >= 4.0)
                            // Don't include the main product itself if found
                            .filter(p -> !p.getName().equalsIgnoreCase(mainProduct.getName()))
                            .sorted((p1, p2) -> Double.compare(p2.getRating(), p1.getRating()))
                            .distinct() // Ensure products are unique by object identity/equals (might need more robust
                                        // distinct if objects diff)
                            // Actually distinct() uses equals(), usually OK if implemented, otherwise
                            // stream
                            // might have dups.
                            // Let's rely on filter mainly.
                            .limit(4)
                            .toList();

                    log.info("Found {} high-quality similar candidates from {} raw items.", similarCandidates.size(),
                            allShallowSimilar.size());

                    // If strict 4+ yields nothing, relax to top 3 generic
                    if (similarCandidates.isEmpty()) {
                        similarCandidates = allShallowSimilar.stream()
                                .filter(p -> !p.getName().equalsIgnoreCase(mainProduct.getName()))
                                .limit(3)
                                .toList();
                        log.warn("Falling back to generic similar candidates: {}", similarCandidates.size());
                    }

                    if (!similarCandidates.isEmpty()) {
                        List<CompletableFuture<Void>> futures = similarCandidates.stream()
                                .map(candidate -> CompletableFuture.runAsync(() -> {
                                    try {
                                        // Deep Scrape for Details (Reviews, etc)
                                        Product p = scraperService.scrapeAmazonOnUrl(candidate.getUrl());

                                        // Restore metadata if lost or missing
                                        if (p.getRating() == 0.0 && candidate.getRating() != null) {
                                            p.setRating(candidate.getRating());
                                        }
                                        if (p.getPrice().equals("N/A") && !candidate.getPrice().equals("N/A")) {
                                            p.setPrice(candidate.getPrice());
                                        }

                                        if (p.isValid()) {
                                            log.info("Analyzing reviews for similar product: {}", p.getName());
                                            analyzeProductReviews(p);

                                            p.setRecommended(false);
                                            synchronized (emitter) {
                                                emitter.send(p);
                                                log.info("Emitted similar product: {}", p.getName());
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.warn("Failed to stream similar product: {}", candidate.getUrl(), e);
                                    }
                                }, taskExecutor))
                                .toList();

                        futures.forEach(CompletableFuture::join);
                        log.info("All similar products processed.");
                    }
                }
            }

            emitter.complete();
            log.info("Stream completed.");

        } catch (Exception e) {
            log.error("Error analyzing link stream", e);
            emitter.completeWithError(e);
        }
    }

    private void saveHistoryForUser(String query, Product product, String username) {
        if (username == null)
            return;
        try {
            User user = userRepository.findByUsername(username);
            if (user != null) {
                ProductHistory history = ProductHistory.builder()
                        .user(user)
                        .searchQuery(query)
                        .productName(product.getName())
                        .productRating(product.getRating())
                        .summary(product.getVerdict())
                        .imageUrl(product.getImageUrl())
                        .productUrl(product.getUrl())
                        .build();
                productHistoryRepository.save(history);
                log.info("Saved product analysis history for user: {}", username);
            }
        } catch (Exception e) {
            log.error("Failed to save product history: ", e);
        }
    }
}
