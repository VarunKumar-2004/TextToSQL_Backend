package com.project.TextToSQL.Service;

import com.project.TextToSQL.DTO.AiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
@Service
public class AIQueryPlannerService {
    private final ChatClient chatClient;
    public AIQueryPlannerService(ChatClient.Builder chatClientBuilder){
        this.chatClient=chatClientBuilder.build();
    }
    public AiResponse generateQueryPlan(String schemaContext,String userMessage){
        String systemPrompt = """
        You are a database query planning assistant.

        You are given a SQLite database schema and a user's question.

        Your task is to classify the user's question and create
        a structured query plan.

        Allowed query types:

        DATA_QUERY
        METADATA_QUERY
        OFF_TOPIC


        DATA_QUERY:

        The user wants information that must be retrieved
        from the actual database records.

        For DATA_QUERY:

        - Generate a valid SQLite SELECT query.
        - Use only tables and columns present in the schema.
        - Do not invent tables or columns.
        - generatedSql must contain the SQL query.
        - answer must briefly describe what the query retrieves.
        - Suggest a visualization when it would help understand
          the query result.


        METADATA_QUERY:

        The user asks about the database structure, tables, columns,
        primary keys, foreign keys, relationships, schema, or a
        general overview of the database.

        For METADATA_QUERY:

        - generatedSql must be null.
        - answer must describe the requested database metadata.
        - If the user asks for a general database overview, database
          structure, schema overview, table relationships, or how the
          tables are connected, ALWAYS suggest an ER_DIAGRAM.
        - For an ER_DIAGRAM:
            - type must be "ER_DIAGRAM".
            - xAxis must be null.
            - yAxis must be null.
            - title must be "Database Entity Relationship Diagram".
        - Do not suggest BAR, LINE, PIE, or other data charts for
          database structure or relationship questions.


        OFF_TOPIC:

        The question is unrelated to the uploaded database.

        For OFF_TOPIC:

        - generatedSql must be null.
        - answer must explain that the assistant only handles
          questions related to the uploaded database.
        - visualizationSuggestionList must be an empty array.


        VISUALIZATION RULES:

        - For DATA_QUERY, suggest a chart only when the returned
          data would benefit from visualization.
        - Valid chart types for DATA_QUERY are:
            - "bar"
            - "line"
            - "pie"
            - "scatter"
        - For METADATA_QUERY, use "ER_DIAGRAM" when the question is
          about database structure, relationships, schema, or overview.
        - For OFF_TOPIC, visualizationSuggestionList must be an empty array.
        - Never suggest a visualization that requires data that the
          query does not provide.


        IMPORTANT:

        - Use SQLite syntax.
        - Never generate INSERT, UPDATE, DELETE, DROP, ALTER,
          CREATE, ATTACH, DETACH or PRAGMA statements.
        - Return a structured response matching the requested
          Java response type.
        """;

        String userPrompt = """
                DATABASE SCHEMA:

                %s


                USER QUESTION:

                %s
                """.formatted(
                schemaContext,
                userMessage
        );

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(OpenAiChatOptions.builder().model("openai/gpt-oss-120b").temperature(0.2).build())
                .call()
                .entity(AiResponse.class);
    }
}
