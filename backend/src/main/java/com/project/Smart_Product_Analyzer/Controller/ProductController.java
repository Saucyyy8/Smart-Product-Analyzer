package com.project.Smart_Product_Analyzer.Controller;

import com.project.Smart_Product_Analyzer.Model.Product;
import com.project.Smart_Product_Analyzer.Model.ProductAnalysisRequest;
import com.project.Smart_Product_Analyzer.Service.ProductService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

@Builder
@Slf4j
@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health Check endpoint called");
        return new ResponseEntity<>("Smart Product Analyzer Running", HttpStatus.OK);
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeProduct(@Valid @RequestBody ProductAnalysisRequest request) {
        log.info("Analyze endpoint called for {}", request);
        try {
            List<Product> productResult;
            if (request.isUrl()) {
                productResult = service.analyzeLink(request.getInput());
            } else
                productResult = service.analyzeProduct(request.getInput());
            log.info("Analyzed Product with response size: {}", productResult.size());
            return ResponseEntity.ok(productResult);
        } catch (Exception e) {
            log.error("Error found during Product Analysis in /analyze endpoint", e);
            Map<String, Object> errorFormat = new HashMap<>();
            errorFormat.put("timestamp: ", LocalDateTime.now());
            errorFormat.put("HttpStatus :", 505);
            errorFormat.put("error :", "Internal Server Error");
            errorFormat.put("message :", e);
            return new ResponseEntity<>(errorFormat, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/analyze/url")
    public ResponseEntity<?> analyzeProductUrl(@Valid @RequestBody ProductAnalysisRequest request) {
        log.info("Product URL analysis request received: {}", request.getInput());

        try {
            List<Product> result = service.analyzeLink(request.getInput());
            log.info("Product URL analysis completed successfully, found: {}", result.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error found during Product Analysis in /analyze/url endpoint", e);
            Map<String, Object> errorFormat = new HashMap<>();
            errorFormat.put("timestamp: ", LocalDateTime.now());
            errorFormat.put("HttpStatus :", 505);
            errorFormat.put("error :", "Internal Server Error");
            errorFormat.put("message :", e);
            return new ResponseEntity<>(errorFormat, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/analyze/description")
    public ResponseEntity<?> analyzeProductDescription(@Valid @RequestBody ProductAnalysisRequest request) {
        log.info("Product description analysis request received: {}", request.getInput());

        try {
            List<Product> result = service.analyzeProduct(request.getInput());
            log.info("Product description analysis completed successfully, found: {}", result.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error found during Product Analysis in /analyze/description endpoint", e);
            Map<String, Object> errorFormat = new HashMap<>();
            errorFormat.put("timestamp: ", LocalDateTime.now());
            errorFormat.put("HttpStatus :", 505);
            errorFormat.put("error :", "Internal Server Error");
            errorFormat.put("message :", e);
            return new ResponseEntity<>(errorFormat, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/stream")
    public SseEmitter streamProductAnalysis(@Valid @RequestBody ProductAnalysisRequest request) {
        log.info("Stream Analyze endpoint called for {}", request.getInput());
        SseEmitter emitter = new SseEmitter(120000L); // 2 minutes timeout

        // Run the heavy lifting in a separate thread so we can return the emitter
        // immediately
        service.analyzeProductStream(request, emitter);

        return emitter;
    }
}
