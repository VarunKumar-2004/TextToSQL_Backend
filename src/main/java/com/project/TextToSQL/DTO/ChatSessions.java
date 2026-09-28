package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ChatSessions {
    private UUID sessionId;
    private String sessionName;
    private String schemaContext;
    LocalDateTime createdAt;
}
