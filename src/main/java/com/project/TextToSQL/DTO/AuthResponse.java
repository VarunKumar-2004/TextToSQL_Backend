package com.project.TextToSQL.DTO;

import com.project.TextToSQL.Model.User;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String token;
    private String message;
    private String email;
}
