package com.project.TextToSQL.Service;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service

public class SQLCorrectionService {
    private final ChatClient chatClient;
    public SQLCorrectionService(ChatClient.Builder chatClientBuilder){
        this.chatClient=chatClientBuilder.build();
    }
    public String correctSQL(String failedSql,String schemaContext,String userMessage,String errorMessage){
        String systemPrompt= """
                You are a SQLite SQL correction assistant.
                
                                The previous SQL query failed during execution.
                
                                Your task is to generate a corrected SQL query.
                
                                Rules:
                                - Return only a valid SQLite SELECT or WITH query.
                                - Use only tables and columns that exist in the provided schema.
                                - Do not invent tables or columns.
                                - Do not use INSERT, UPDATE, DELETE, DROP, ALTER,
                                  CREATE, ATTACH, DETACH or PRAGMA.
                                - Fix the SQL based on the SQLite execution error.
                                - Do not explain the correction.
                                - Return only the corrected SQL query.
                """;
        String userPrompt = """
                DATABASE SCHEMA:

                %s

                USER QUESTION:

                %s

                FAILED SQL:

                %s

                SQLITE ERROR:

                %s

                Generate the corrected SQLite query.
                """.formatted(
                schemaContext,
                userMessage,
                failedSql,
                errorMessage
        );
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(OpenAiChatOptions.builder().model("openai/gpt-oss-20b").temperature(0.2).build())
                .call()
                .content()
                .trim();
    }
}
