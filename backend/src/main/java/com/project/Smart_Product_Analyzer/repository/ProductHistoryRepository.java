package com.project.Smart_Product_Analyzer.repository;

import com.project.Smart_Product_Analyzer.entity.ProductHistory;
import com.project.Smart_Product_Analyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Long> {
    List<ProductHistory> findByUserOrderByCreatedAtDesc(User user);

    // For caching: check if we have a recent history for this query or product name
    ProductHistory findTopBySearchQueryOrProductNameOrderByCreatedAtDesc(String searchQuery, String productName);
}
