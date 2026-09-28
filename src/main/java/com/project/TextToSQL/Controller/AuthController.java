package com.project.TextToSQL.Controller;

import com.project.TextToSQL.DTO.AuthRequest;
import com.project.TextToSQL.DTO.AuthResponse;
import com.project.TextToSQL.DTO.UserResponse;
import com.project.TextToSQL.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthResponse>register(@Valid @RequestBody AuthRequest authrequest){
           AuthResponse response=authService.register(authrequest);
           return ResponseEntity.status(HttpStatus.CREATED).body(response) ;
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse>login(@Valid @RequestBody AuthRequest request){
        AuthResponse response=authService.login(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse>getCurrentUser(Authentication authentication){
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }
}
