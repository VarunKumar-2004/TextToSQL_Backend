package com.project.TextToSQL.Model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Entity
@Table(name="chat_sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatSession {
    @Id
    private UUID id;
    @Column(name="session_name")
    private String sessionName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name="schema_context",columnDefinition = "TEXT")
    private String schemaContext;
    @Column(name = "database_file_path", nullable = false)
    private String databaseFilePath;
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;
}
