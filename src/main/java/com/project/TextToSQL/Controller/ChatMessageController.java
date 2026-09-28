package com.project.TextToSQL.Controller;

import com.project.TextToSQL.DTO.*;
import com.project.TextToSQL.Model.ChatSession;
import com.project.TextToSQL.Model.User;
import com.project.TextToSQL.Repository.UserRepository;
import com.project.TextToSQL.Service.AIQueryPlannerService;
import com.project.TextToSQL.Service.ChatMessageService;
import com.project.TextToSQL.Service.SQLValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat/sessions")
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<?>sendMessage(@PathVariable UUID sessionId, @RequestBody ChatMessageRequest request, Authentication authentication) throws Exception{

        ChatExecutionResponse response=chatMessageService.processMessage(sessionId,authentication.getName(),request.getMessage());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{sessionId}/messages/{messageId}/results")
    public ResponseEntity<QueryResultPage>getQueryResults(
            @PathVariable UUID sessionId,
            @PathVariable UUID messageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            Authentication authentication
    )throws Exception{
        QueryResultPage results=chatMessageService.getQueryResults(sessionId,messageId,authentication.getName(),page,pageSize);
        return ResponseEntity.ok(results);
    }

}
