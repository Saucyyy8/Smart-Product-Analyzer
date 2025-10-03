package com.project.Smart_Product_Analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SmartProductAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartProductAnalyzerApplication.class, args);
	}

}
