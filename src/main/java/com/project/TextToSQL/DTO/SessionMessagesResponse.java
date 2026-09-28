package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
@AllArgsConstructor
public class SessionMessagesResponse {
   private boolean success;
   private List<SessionMessages> messages;
}
