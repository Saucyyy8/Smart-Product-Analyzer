package com.project.Smart_Product_Analyzer.Service;

import com.project.Smart_Product_Analyzer.Exception.InvalidUrlException;
import com.project.Smart_Product_Analyzer.Exception.ProductNotFound;
import com.project.Smart_Product_Analyzer.Exception.ScrapingException;
import com.project.Smart_Product_Analyzer.Model.Product;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class AmazonSearchPageScraper {

    private final WebDriver driver;

    public AmazonSearchPageScraper(WebDriver driver) {
        this.driver = driver;
    }

    public List<Product> scrapeSearchPage(String url) {

        log.info("Starting Shallow Scrape from url : {}", url);
        validateUrl(url);
        try {
            driver.get(url);
            try {
                // Random Sleep (2s - 4s)
                long sleepTime = 2000 + (long) (Math.random() * 2000);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Quick wait for results
            new WebDriverWait(driver, Duration.ofSeconds(5));

            List<Product> products = extractProductsFromSearch();

            if (products.isEmpty()) {
                String pageSource = driver.getPageSource().toLowerCase();
                if (pageSource.contains("captcha") || pageSource.contains("enter the characters you see below")) {
                    log.error("CAPTCHA DETECTED: Amazon is blocking the scraper on URL: {}", url);
                } else {
                    log.warn("No products found, but no explicit CAPTCHA detected on URL: {}", url);
                }
            }

            log.info("Extracted {} products with metadata from search page.", products.size());
            return products;

        } catch (Exception e) {
            log.error("Error in shallow scraping : ", e);
            throw new ScrapingException("Error in shallow scraping", e);
        }
    }

    // Kept for backward compatibility if needed, but delegates to new logic
    public List<String> scrapeAmazonOnUrl(String url) {
        List<Product> products = scrapeSearchPage(url);
        return products.stream().map(Product::getUrl).toList();
    }

    public void validateUrl(String url) {
        try {
            URL urlToParse = new URL(url);
            String host = urlToParse.getHost().toLowerCase();
            Set<String> amazonUrls = new HashSet<>();
            amazonUrls.add("www.amazon.in");
            amazonUrls.add("www.amazon.com");
            if (!amazonUrls.contains(host)) {
                throw new InvalidUrlException("Invalid Url");
            }
        } catch (Exception e) {
            throw new InvalidUrlException("Invalid Url" + e.getMessage());
        }
    }

    public List<Product> extractProductsFromSearch() {
        List<Product> products = new ArrayList<>();
        Set<String> uniqueAsins = new HashSet<>();

        // Try to find any items with an ASIN (most reliable way to find products)
        List<WebElement> cards = driver.findElements(By.cssSelector("div[data-asin]"));

        if (cards.isEmpty()) {
            // Fallback to strict class if data-asin is somehow missing (unlikely for
            // products)
            cards = driver.findElements(By.cssSelector("div[data-component-type='s-search-result']"));
        }

        log.debug("Found {} potential product cards.", cards.size());

        for (WebElement card : cards) {
            try {
                // Skip items without an ASIN (empty attribute)
                String asin = card.getAttribute("data-asin");
                if (asin == null || asin.trim().isEmpty()) {
                    continue;
                }

                if (uniqueAsins.contains(asin))
                    continue;

                // 1. Name & URL
                WebElement titleElement = null;
                WebElement linkElement = null;

                try {
                    // Organic: h2 > a
                    titleElement = card.findElement(By.cssSelector("h2"));
                    List<WebElement> links = titleElement.findElements(By.tagName("a"));
                    if (!links.isEmpty()) {
                        linkElement = links.get(0);
                    } else {
                        // Sponsored/Alternative: a > h2
                        // Get parent of h2
                        linkElement = titleElement.findElement(By.xpath("./.."));
                        // Check if parent is actually an anchor
                        if (!"a".equalsIgnoreCase(linkElement.getTagName())) {
                            linkElement = null; // not a link
                        }
                    }
                } catch (Exception e) {
                    log.trace("Failed to find title/link for card: {}", e.getMessage());
                }

                if (titleElement == null || linkElement == null)
                    continue;

                String name = titleElement.getText();
                String href = linkElement.getAttribute("href");

                // Validate URL
                if (href == null || href.isEmpty() || href.contains("javascript:") || href.endsWith("#")) {
                    // Try getting data-href or fallback
                    continue;
                }

                String fullUrl = completeUrl(href);
                // Double check it's not the search page itself
                if (fullUrl.contains("/s?k=") && !fullUrl.contains("/dp/") && !fullUrl.contains("/gp/")) {
                    log.debug("Skipping search result that points to another search: {}", fullUrl);
                    continue;
                }

                // 2. Rating
                Double rating = 0.0;
                try {
                    // Try explicit rating span first
                    WebElement ratingElement = card.findElement(By.cssSelector("span[aria-label*='out of 5 stars']"));
                    String ratingText = ratingElement.getAttribute("aria-label");

                    if (ratingText != null) {
                        String ratingNum = ratingText.split(" ")[0];
                        rating = Double.parseDouble(ratingNum);
                    }
                } catch (Exception e) {
                    // Rating is often missing for sponsored/new items
                }

                // 3. Price
                String price = "N/A";
                try {
                    // Try hidden offscreen price which is standard
                    WebElement priceElement = card.findElement(By.cssSelector("span.a-price span.a-offscreen"));
                    price = priceElement.getAttribute("innerText");
                } catch (Exception e) {
                    // Price might be missing
                }

                // Create Product Object
                Product p = Product.builder()
                        .name(name)
                        .url(fullUrl)
                        .rating(rating)
                        .price(price)
                        // Initialize empty lists
                        .pros(new ArrayList<>())
                        .cons(new ArrayList<>())
                        .verdict("Pending Analysis")
                        .build();

                products.add(p);
                uniqueAsins.add(asin);

                if (products.size() >= 15)
                    break;

            } catch (Exception e) {
                // Skip bad cards but continue
                log.trace("Skipping a card due to error: {}", e.getMessage());
            }
        }

        return products;
    }

    private String extractAsin(String url) {
        // Simple extraction for uniqueness check
        try {
            if (url.contains("/dp/")) {
                int index = url.indexOf("/dp/");
                return url.substring(index + 4, index + 14);
            }
        } catch (Exception e) {
        }
        return url; // fallback to full url
    }

    private String completeUrl(String href) {
        if (href.startsWith("http")) {
            return href;
        }
        return "https://www.amazon.in" + href;
    }
}
// String[] selectors = {};