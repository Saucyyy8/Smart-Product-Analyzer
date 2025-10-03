package com.project.Smart_Product_Analyzer.Exception;

public class ScrapingException extends RuntimeException{
    public ScrapingException(String errorInScrapingProducts, Exception e) {
        super(errorInScrapingProducts);
    }
}
