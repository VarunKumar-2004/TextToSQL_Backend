package com.project.TextToSQL.Controller;

import com.project.TextToSQL.DTO.ChatSessions;
import com.project.TextToSQL.DTO.ChatSessionsResponse;

import com.project.TextToSQL.DTO.SchemaResponse;
import com.project.TextToSQL.DTO.SessionMessagesResponse;
import com.project.TextToSQL.Service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat/sessions")
@RequiredArgsConstructor
public class ChatSessionController {
    private final ChatSessionService chatSessionService;

    @GetMapping
    public ResponseEntity<ChatSessionsResponse>getMySessions(Authentication authentication){
        ChatSessionsResponse response = chatSessionService.getMySessions(authentication.getName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/upload")
    public ResponseEntity<ChatSessions> createSession(@RequestParam("file")MultipartFile file,@RequestParam("sessionName") String sessionName, Authentication authentication) throws IOException{

        ChatSessions response=chatSessionService.createSession(authentication.getName(),file,sessionName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionMessagesResponse>getSessionMessages(@PathVariable UUID sessionId,Authentication authentication){
         SessionMessagesResponse list=chatSessionService.getSessionMessages(sessionId,authentication.getName());
         return ResponseEntity.ok(list);
    }
    @GetMapping("/{sessionId}/schema")
    public ResponseEntity<SchemaResponse> getSchema(@PathVariable UUID sessionId,Authentication authentication){
        return ResponseEntity.ok(chatSessionService.getSchemaContext(sessionId,authentication.getName()));
    }
}
