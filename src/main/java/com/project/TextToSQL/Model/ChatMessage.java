package com.project.TextToSQL.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(name = "user_prompt", columnDefinition = "TEXT", nullable = false)
    private String userPrompt;

    @Column(name = "generated_sql", columnDefinition = "TEXT")
    private String generatedSql;
    @Column(name="answer",columnDefinition = "TEXT")
    private String answer;
    @Column(name="query_result",columnDefinition = "TEXT")
    private String queryResult;
    @Column(name="visualizations_list",columnDefinition = "Text")
    private String visualizationSuggestions;
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}