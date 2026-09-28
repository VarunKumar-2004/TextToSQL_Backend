package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiResponse {
    private String queryType;
    private String generatedSql;
    private String answer;
    private List<VisualizationSuggestion> visualizationSuggestionList;
}
