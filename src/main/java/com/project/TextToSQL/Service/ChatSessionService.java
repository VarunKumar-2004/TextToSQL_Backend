package com.project.TextToSQL.Service;

import com.project.TextToSQL.DTO.*;
import com.project.TextToSQL.Model.ChatMessage;
import com.project.TextToSQL.Model.ChatSession;
import com.project.TextToSQL.Model.User;
import com.project.TextToSQL.Repository.ChatMessageRepository;
import com.project.TextToSQL.Repository.ChatSessionRepository;
import com.project.TextToSQL.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class ChatSessionService {
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final DatabaseStorageService databaseStorageService;
    private final DatabaseSchemaService databaseSchemaService;
    private final ChatMessageRepository chatMessageRepository;
    private final SchemaMapper schemaMapper;
    public List<ChatSession> getSessions(UUID userId){
        return chatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private void validateDatabaseFile(MultipartFile file) {

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "Database file name is missing"
            );
        }

        String lowerCaseName = filename.toLowerCase();

        if (!lowerCaseName.endsWith(".db")
                && !lowerCaseName.endsWith(".sqlite")
                && !lowerCaseName.endsWith(".sqlite3")) {

            throw new IllegalArgumentException(
                    "Only .db, .sqlite and .sqlite3 files are supported"
            );
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Database file is empty"
            );
        }
    }

    public ChatSessions createSession(String email, MultipartFile file,String sessionName) throws IOException {
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));
        validateDatabaseFile(file);
        UUID sessionId=UUID.randomUUID();
        String databaseFilePath=databaseStorageService.uploadDatabase(file,user.getId(),sessionId);
        String schemaContext=databaseSchemaService.extractSchema(file);
        ChatSession session=ChatSession.builder()
                .id(sessionId)
                .sessionName(sessionName)
                .user(user)
                .databaseFilePath(databaseFilePath)
                .schemaContext(schemaContext)
                .build();
        ChatSession sessionx=chatSessionRepository.save(session);
        ChatSessions response=new ChatSessions(sessionx.getId(),sessionx.getSessionName(),sessionx.getSchemaContext(),sessionx.getCreatedAt());
        return response;
    }

    public ChatSession getSession(UUID sessionId,String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));
        return chatSessionRepository.findByIdAndUserId(sessionId,user.getId()).orElseThrow(()->new RuntimeException("No chat session found"));
    }

    public ChatSessionsResponse getMySessions(String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user Not Found"));
        List<ChatSession> list=getSessions(user.getId());
        List<ChatSessions> chatSessions= list.stream()
                .map(session -> new ChatSessions(
                        session.getId(),
                        session.getSessionName(),
                        session.getSchemaContext(),
                        session.getCreatedAt()
                ))
                .toList();
        return new ChatSessionsResponse(true,chatSessions);
    }

    public SessionMessagesResponse getSessionMessages(UUID sessionId,String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
        ChatSession session=chatSessionRepository.findByIdAndUserId(sessionId,user.getId()).orElseThrow(()->new RuntimeException("Chat session not found"));
        List<ChatMessage> messages=chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<SessionMessages> sessionMessages= messages.stream()
                .map(message->new SessionMessages(
                        message.getId(),
                        session.getId(),
                        message.getUserPrompt(),
                        message.getAnswer(),
                        message.getQueryResult(),
                        message.getVisualizationSuggestions()
                )).toList();
        return new SessionMessagesResponse(true,sessionMessages);
    }
    public SchemaResponse getSchemaContext(UUID sessionId,String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId,user.getId()).orElseThrow(()->new RuntimeException("session not found"));
        return schemaMapper.toResponse(session.getSchemaContext());

    }
}
