package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ChatMessageResponse {

    private UUID id;
    private UUID sessionId;
    private String userPrompt;
    private String generatedSql;
    private String answer;
    private String visualizationSuggestions;
    private LocalDateTime createdAt;
}