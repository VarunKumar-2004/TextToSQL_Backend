package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class APIErrorResponse {
     private boolean success;
     private String message;
     private String errorCode;
     private LocalDateTime timestamp;
}
