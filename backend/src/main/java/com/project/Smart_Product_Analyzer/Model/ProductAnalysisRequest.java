package com.project.Smart_Product_Analyzer.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAnalysisRequest {
    @NotNull(message = "Input should not be Empty")
    @Size(min = 3, max = 1000, message = "Input not within limits")
    private String input;
    private String type;

    public boolean isUrl(){
        return (input!=null && input.startsWith("https"));
    }
    public boolean isDescription(){
        return (input!=null && !input.startsWith("https"));
    }

}
