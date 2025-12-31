package com.project.Smart_Product_Analyzer.Service;

import com.project.Smart_Product_Analyzer.Exception.ProductNotFound;
import com.project.Smart_Product_Analyzer.Exception.ScrapingException;
import com.project.Smart_Product_Analyzer.Model.Product;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class AmazonScraperService {

    private final ObjectFactory<WebDriver> webDriverFactory;

    @Autowired
    public AmazonScraperService(ObjectFactory<WebDriver> webDriverFactory) {
        this.webDriverFactory = webDriverFactory;
    }

    public Product scrapeAmazonOnUrl(String url) {
        log.info("Starting to scrape product from URL: {}", url);

        WebDriver webDriver = null;
        try {
            webDriver = webDriverFactory.getObject();

            Product product = Product.builder()
                    .url(url)
                    .build();

            // Navigate to the URL first
            webDriver.get(url);
            log.info("Navigated to URL: {}", url);

            // Random Sleep to mimic human behavior (2s - 4s)
            try {
                long sleepTime = 2000 + (long) (Math.random() * 2000);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            product.setName(extractProductName(webDriver, wait));

            product.setPrice(extractProductPrice(webDriver, wait));
            product.setImageUrl(extractProductImage(webDriver, wait));

            List<String> reviews = extractProductReviews(webDriver, wait);
            product.setPros(reviews);
            log.info("Successfully Scraped Product : {}", product.getName());
            return product;

        } catch (Exception e) {
            log.error("Error in scraping product", e);
            throw new ScrapingException("Error in Scraping product", e);
        } finally {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.error("Error quitting WebDriver", e);
                }
            }
        }
    }

    private String extractProductPrice(WebDriver webDriver, WebDriverWait wait) {
        try {
            // Robust price selectors
            String[] priceSelectors = {
                    "#corePrice_feature_div .a-price .a-offscreen",
                    "#corePriceDisplay_desktop_feature_div .a-price .a-offscreen",
                    ".a-price .a-offscreen",
                    "#priceblock_ourprice",
                    "#priceblock_dealprice",
                    ".a-price-whole" // Fallback to just the whole number part
            };

            for (String selector : priceSelectors) {
                try {
                    List<WebElement> elements = webDriver.findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        String price = elements.get(0).getAttribute("innerText");
                        if (price == null || price.trim().isEmpty()) {
                            price = elements.get(0).getText();
                        }

                        if (price != null && !price.trim().isEmpty()) {
                            log.debug("Extracted product price: {}", price);
                            return price.trim();
                        }
                    }
                } catch (Exception e) {
                    log.debug("Price selector {} failed, trying next", selector);
                }
            }

            log.warn("All price selectors failed");
            return "N/A";
        } catch (Exception e) {
            log.error("Failed to extract product price: ", e);
            return "N/A";
        }
    }

    private List<String> extractProductReviews(WebDriver webDriver, WebDriverWait wait) {
        try {
            log.info("Starting review extraction for URL: {}", webDriver.getCurrentUrl());

            Set<String> reviewSet = new LinkedHashSet<>(); // Preserve order
            extractReviewsSimple(webDriver, wait, reviewSet);

            List<String> reviews = new ArrayList<>(reviewSet);
            if (!reviewSet.isEmpty()) {
                log.info("Found {} reviews", reviewSet.size());
                return reviews;
            }

            log.warn("No reviews found for product");
            return Collections.singletonList("No reviews available for this product.");

        } catch (Exception e) {
            log.error("Failed to extract product reviews: ", e);
            return Collections.singletonList("Failed to extract reviews.");
        }
    }

    private void extractReviewsSimple(WebDriver webDriver, WebDriverWait wait, Set<String> reviews) {
        log.info("Extracing reviews...");
        try {
            // Selectors for review bodies
            String[] reviewSelectors = {
                    "div[data-hook='review-collapsed']",
                    "span[data-hook='review-body'] span",
                    ".review-text-content span",
                    "#feature-bullets li span.a-list-item" // Fallback to features if no reviews
            };

            for (String selector : reviewSelectors) {
                try {
                    List<WebElement> elements = webDriver.findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        log.info("Found {} elements with selector: {}", elements.size(), selector);
                        for (WebElement element : elements) {
                            String text = element.getAttribute("innerText"); // explicit text
                            if (text == null || text.isEmpty())
                                text = element.getText();

                            if (isValidReview(text)) {
                                reviews.add(text.trim());
                                if (reviews.size() >= 10)
                                    return; // Limit to 10 reviews
                            }
                        }
                        if (!reviews.isEmpty())
                            return;
                    }
                } catch (Exception e) {
                    log.debug("Selector {} failed", selector);
                }
            }
        } catch (Exception e) {
            log.error("Simple review extraction failed", e);
        }
    }

    private boolean isValidReview(String text) {
        if (text == null)
            return false;
        String t = text.trim();
        return t.length() > 20 && // Increase min length
                !t.contains("Verified Purchase") &&
                !t.contains("Helpful") &&
                !t.contains("Report") &&
                !t.contains("stars") &&
                !t.contains("out of 5") &&
                !t.contains("customer reviews") &&
                !t.contains("See all") &&
                !t.contains("Write a review");

    }

    private @NotBlank(message = "Product name is required") String extractProductName(WebDriver webDriver,
            WebDriverWait wait) {
        try {
            // Robust product name selectors
            String[] nameSelectors = {
                    "#productTitle",
                    "#title",
                    "h1#title",
                    "#feature-title",
                    ".product-title-word-break"
            };

            for (String selector : nameSelectors) {
                try {
                    List<WebElement> elements = webDriver.findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        String name = elements.get(0).getText().trim();
                        if (!name.isEmpty()) {
                            log.info("Got product Name: {}", name);
                            return name;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Name selector {} failed, trying next", selector);
                }
            }

            log.error("All name selectors failed");
            // Don't throw exception immediately, try to return a fallback or re-use URL
            // segments?
            // For now, throw to trigger the error handling loop, but maybe we should relax
            // this?
            throw new ProductNotFound("Product Name not found");
        } catch (Exception e) {
            throw new ProductNotFound("Product Name not found");
        }
    }

    private String extractProductImage(WebDriver webDriver, WebDriverWait wait) {
        try {
            String[] imageSelectors = {
                    "#landingImage",
                    "#imgTagWrapperId img",
                    "#main-image",
                    ".a-dynamic-image",
                    "img[data-a-image-name='landingImage']"
            };

            for (String selector : imageSelectors) {
                try {
                    List<WebElement> elements = webDriver.findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        String src = elements.get(0).getAttribute("src");
                        if (src != null && !src.isEmpty()) {
                            return src;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Image selector {} failed", selector);
                }
            }
            return "https://via.placeholder.com/300?text=No+Image"; // Fallback image
        } catch (Exception e) {
            log.warn("Failed to extract product image", e);
            return null;
        }
    }
}
