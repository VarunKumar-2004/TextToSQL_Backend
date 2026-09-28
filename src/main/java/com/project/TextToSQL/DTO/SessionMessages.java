package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class SessionMessages {
    private final UUID messageId;
    private final UUID sessionId;
    private final String userPrompt;
    private final String answer;
    private final String queryResult;
    private final String visualizationSuggestion;
}
