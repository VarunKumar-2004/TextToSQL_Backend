package com.project.TextToSQL.DTO;

import com.project.TextToSQL.DTO.AiResponse;
import com.project.TextToSQL.Model.ChatMessage;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
@Getter
@RequiredArgsConstructor
public class ChatExecutionResponse{
    private final ChatMessageResponse response;
    private final QueryResultPage queryResultPage;
}