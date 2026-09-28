package com.project.TextToSQL.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageRequest {
    @NotBlank(message = "Message cannot be empty")
    @Size(max=2000,message = "Message cannot exceed 2000 characters")
    private String message;
}
