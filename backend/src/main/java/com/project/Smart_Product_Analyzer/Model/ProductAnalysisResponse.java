package com.project.Smart_Product_Analyzer.Model;

import java.util.List;

public class ProductAnalysisResponse {

    private String analysis;
    private Product bestProduct;
    private List<Product> otherProducts;

    public ProductAnalysisResponse(String analysis, Product bestProduct, List<Product> otherProducts) {
        this.analysis = analysis;
        this.bestProduct = bestProduct;
        this.otherProducts = otherProducts;
    }

    // Getters and Setters
    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public Product getBestProduct() {
        return bestProduct;
    }

    public void setBestProduct(Product bestProduct) {
        this.bestProduct = bestProduct;
    }

    public List<Product> getOtherProducts() {
        return otherProducts;
    }

    public void setOtherProducts(List<Product> otherProducts) {
        this.otherProducts = otherProducts;
    }
}
