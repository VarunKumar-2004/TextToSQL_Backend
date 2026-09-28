package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VisualizationSuggestion {
    private String type;
    private String xAxis;
    private String yAxis;
    private String title;
}
