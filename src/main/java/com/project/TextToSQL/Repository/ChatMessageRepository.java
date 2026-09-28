package com.project.TextToSQL.Repository;

import com.project.TextToSQL.Model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID chat_session_id);
    Optional<ChatMessage>findByIdAndSessionId(UUID messageId,UUID sessionId);

}
