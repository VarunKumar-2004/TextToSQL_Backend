package com.project.TextToSQL.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.TextToSQL.DTO.AiResponse;
import com.project.TextToSQL.DTO.ChatExecutionResponse;
import com.project.TextToSQL.DTO.ChatMessageResponse;
import com.project.TextToSQL.DTO.QueryResultPage;
import com.project.TextToSQL.Model.ChatMessage;
import com.project.TextToSQL.Model.ChatSession;
import com.project.TextToSQL.Model.User;
import com.project.TextToSQL.Repository.ChatMessageRepository;
import com.project.TextToSQL.Repository.ChatSessionRepository;
import com.project.TextToSQL.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AIQueryPlannerService aiQueryPlannerService;
    private final SQLValidationService sqlValidationService;
    private final DatabaseSessionManager databaseSessionManager;
    private final SQLiteQueryExecutor sqLiteQueryExecutor;
    private final SQLCorrectionService sqlCorrectionService;
    private final ObjectMapper objectMapper;
    private final AIResultSummarization aiResultSummarization;
    private final UserRepository userRepository;
    public ChatSession getSessionForMessage(UUID sessionId,UUID userId,String message){
        ChatSession session=chatSessionRepository.findByIdAndUserId(sessionId,userId).orElseThrow(()->new RuntimeException("chat session not found"));
        return session;
    }
    public ChatExecutionResponse processMessage(UUID sessionId,String email,String question) throws Exception{
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));
        ChatSession session=chatSessionRepository.findByIdAndUserId(sessionId,user.getId()).orElseThrow(()->new RuntimeException("chat session not found"));
        AiResponse airesponse=aiQueryPlannerService.generateQueryPlan(session.getSchemaContext(),question);
        String visualizationJson= objectMapper.writeValueAsString(airesponse.getVisualizationSuggestionList());
        if("OFF_TOPIC".equals(airesponse.getQueryType())){
            ChatMessage message=ChatMessage.builder()
                    .session(session)
                    .userPrompt(question)
                    .generatedSql(null)
                    .answer(airesponse.getAnswer())
                    .build();
            ChatMessage savedMessage= chatMessageRepository.save(message);
            ChatMessageResponse response=new ChatMessageResponse(
                    savedMessage.getId(),
                    session.getId(),
                    savedMessage.getUserPrompt(),
                    savedMessage.getGeneratedSql(),
                    savedMessage.getAnswer(),
                    savedMessage.getQueryResult(),
                    savedMessage.getCreatedAt()
            );
            return new ChatExecutionResponse(response,null);
        }
        if("METADATA_QUERY".equals(airesponse.getQueryType())){

            ChatMessage message=ChatMessage.builder()
                    .session(session)
                    .userPrompt(question)
                    .generatedSql(null)
                    .answer(airesponse.getAnswer())
                    .visualizationSuggestions(visualizationJson)
                    .build();
            ChatMessage savedMessage=chatMessageRepository.save(message);
            ChatMessageResponse response=new ChatMessageResponse(
                    savedMessage.getId(),
                    session.getId(),
                    savedMessage.getUserPrompt(),
                    savedMessage.getGeneratedSql(),
                    savedMessage.getAnswer(),
                    savedMessage.getVisualizationSuggestions(),
                    savedMessage.getCreatedAt()
            );
            return new ChatExecutionResponse(response,null);
        }
        if("DATA_QUERY".equals(airesponse.getQueryType())){
            int maxAttempts=3;
            String sql=airesponse.getGeneratedSql();
            System.out.println(sql);
            Path databasePath=databaseSessionManager.getDatabase(session);
            sqlValidationService.validateSql(sql);
            QueryResultPage resultPage=null;
            for (int attempt = 1; attempt<=maxAttempts; attempt++) {
                try {
                    resultPage = sqLiteQueryExecutor.executePage(databasePath, sql, 0, 50);
                    break;
                } catch (Exception exception) {
                    if (attempt == maxAttempts) {
                        throw new RuntimeException("Unable to execute after " + maxAttempts + "attempts", exception);
                    }
                    sql = sqlCorrectionService.correctSQL(sql, session.getSchemaContext(), question, exception.getMessage());
                    sqlValidationService.validateSql(sql);
                    airesponse.setGeneratedSql(sql);
                }
            }
            String answer;
            try{
                answer=aiResultSummarization.AISummarize(question,sql,resultPage.getRows());
            }catch (Exception e){
                answer="The generated querry executed successfully,but iam unable to summarize it";
            }
            String resultsJson=objectMapper.writeValueAsString(resultPage);
            ChatMessage message=ChatMessage.builder()
                    .session(session)
                    .userPrompt(question)
                    .generatedSql(sql)
                    .answer(answer)
                    .visualizationSuggestions(visualizationJson)
                    .queryResult(resultsJson)
                    .build();
            ChatMessage savedMessage= chatMessageRepository.save(message);
            ChatMessageResponse response=new ChatMessageResponse(
                    savedMessage.getId(),
                    session.getId(),
                    savedMessage.getUserPrompt(),
                    savedMessage.getGeneratedSql(),
                    savedMessage.getAnswer(),
                    savedMessage.getVisualizationSuggestions(),
                    savedMessage.getCreatedAt()
            );
            return new ChatExecutionResponse(response,resultPage);
        }
        throw new IllegalArgumentException("UnKnownQueryType");
    }
    public QueryResultPage getQueryResults(UUID sessionId,UUID messageId,String email,int page,int pageSize)throws Exception{
        if(pageSize<1||pageSize>50){
            throw new RuntimeException("page size should be less than or equal to 50");
        }
        if(page<0){
            throw new RuntimeException("page value should be positive");
        }
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));
        ChatSession session=chatSessionRepository.findByIdAndUserId(sessionId,user.getId()).orElseThrow(()->new RuntimeException("chat session not found"));
        Path databasePath=databaseSessionManager.getDatabase(session);
        ChatMessage message=chatMessageRepository.findByIdAndSessionId(messageId,sessionId).orElseThrow(()->new RuntimeException("message not found"));
        QueryResultPage resultPage=sqLiteQueryExecutor.executePage(databasePath,message.getGeneratedSql(),page,pageSize);
        return resultPage;

    }

}
