package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private boolean success;
    private String email;
}
