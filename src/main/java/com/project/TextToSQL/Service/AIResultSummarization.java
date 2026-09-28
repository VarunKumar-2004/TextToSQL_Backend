package com.project.TextToSQL.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AIResultSummarization {
    private final ChatClient chatClient;
    public AIResultSummarization(ChatClient.Builder chatClient){
        this.chatClient=chatClient.build();
    }
    public String AISummarize(String userQuestion, String sql, List<Map<String,Object>> results){
        if (results == null || results.isEmpty()) {
            return "The query executed successfully, but no matching records were found.";
        }
        String resultContext = buildResultContext(results);
        String systemPrompt = """
                You are a database result summarization assistant.

                Your job is to explain SQL query results to the user
                in a concise and useful natural-language answer.

                Rules:
                1. Use ONLY the provided query result.
                2. Do NOT invent values, records, facts, or conclusions.
                3. Do NOT assume that the provided rows represent the
                   complete dataset unless the result itself proves it.
                4. Directly answer the user's question.
                5. Mention important values, names, counts, or comparisons
                   when they are explicitly present in the result.
                6. If the result contains many rows, summarize the result
                   instead of listing every row.
                7. Do not explain the SQL unless it helps answer the question.
                8. Keep the response concise.
                9. Do not mention that you are an AI.
                10. Do not say that there are no more records unless the
                    provided result proves that.
                """;

        String userPrompt = """
                USER QUESTION:
                %s

                SQL QUERY:
                %s

                QUERY RESULT:
                %s

                Based only on the query result above, provide a concise
                natural-language answer to the user's question.
                """.formatted(
                userQuestion,
                sql,
                resultContext
        );

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(
                        OpenAiChatOptions.builder()
                                .model("openai/gpt-oss-20b")
                                .temperature(0.2)
                                .build()
                )
                .call()
                .content()
                .trim();
    }
    private String buildResultContext(
            List<Map<String, Object>> results
    ) {

        int maxRows = 50;

        int rowsToInclude =
                Math.min(results.size(), maxRows);

        return results
                .subList(0, rowsToInclude)
                .toString();
    }

}
