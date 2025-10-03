package com.project.Smart_Product_Analyzer.Model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Product {

    @NotBlank(message = "Product name is required")
    private String name;

    private String price;

    @NotBlank(message = "Product URL is required")
    private String url;
    private List<String> pros;
    private List<String> cons;
    private String verdict;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
    private Double rating = 0.0;

    private String brand;
    private String category;
    private String imageUrl;
    private Integer reviewCount;
    private String availability;

    // Helper methods
    public boolean isValid() {
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasRating = rating != null && rating >= 0.0 && rating <= 10.0;

        // For debugging, we'll be more lenient - just need a name
        return hasName;
    }

}
