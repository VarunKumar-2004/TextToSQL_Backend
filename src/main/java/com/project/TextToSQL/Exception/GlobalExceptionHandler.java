package com.project.TextToSQL.Exception;

import com.project.TextToSQL.DTO.APIErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log= LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIErrorResponse>handleResourceNotFoundException(ResourceNotFoundException ex){
        APIErrorResponse response=new APIErrorResponse(
                false,
                ex.getMessage(),
                "RESOURCE_NOT_FOUND",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<APIErrorResponse>handleInvalidRequestException(InvalidRequestException er){
        APIErrorResponse response=new APIErrorResponse(
                false,
                er.getMessage(),
                "INVALID_REQUEST",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<APIErrorResponse>handleFileValidationException(FileValidationException ex){
        APIErrorResponse response=new APIErrorResponse(
                false,
                ex.getMessage(),
                "INVALID_DATABASE_FILE",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIErrorResponse>handleValidationException(MethodArgumentNotValidException ex){
        String message=ex.getBindingResult()
                .getFieldErrors()
                .stream().findFirst().map(error->error.getDefaultMessage()).orElse("Invalid request");
        APIErrorResponse response=new APIErrorResponse(
                false,
                message,
                "VALIDATION_ERROR",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<APIErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex
    ) {

        APIErrorResponse response = new APIErrorResponse(
                false,
                "Database file is too large. Maximum allowed size is 50MB.",
                "FILE_TOO_LARGE",
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIErrorResponse> handleUnexpectedException(Exception ex){
        log.error("UnexpectedError occured",ex);
        APIErrorResponse response=new APIErrorResponse(
                false,
                "Something went wrong,please try again later",
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
