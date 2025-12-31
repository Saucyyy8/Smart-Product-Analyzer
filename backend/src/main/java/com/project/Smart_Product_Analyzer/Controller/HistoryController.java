package com.project.Smart_Product_Analyzer.Controller;

import com.project.Smart_Product_Analyzer.repository.ProductHistoryRepository;
import com.project.Smart_Product_Analyzer.entity.ProductHistory;
import com.project.Smart_Product_Analyzer.entity.User;
import com.project.Smart_Product_Analyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/history")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class HistoryController {

    private final ProductHistoryRepository productHistoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public HistoryController(ProductHistoryRepository productHistoryRepository, UserRepository userRepository) {
        this.productHistoryRepository = productHistoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductHistory>> getUserHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ProductHistory> history = productHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(history);
    }
}
