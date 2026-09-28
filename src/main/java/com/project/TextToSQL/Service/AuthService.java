package com.project.TextToSQL.Service;

import com.project.TextToSQL.DTO.AuthRequest;
import com.project.TextToSQL.DTO.AuthResponse;
import com.project.TextToSQL.DTO.UserResponse;
import com.project.TextToSQL.Model.User;
import com.project.TextToSQL.Repository.UserRepository;
import com.project.TextToSQL.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(AuthRequest request){
        String email=request.getEmail();
        if(userRepository.findByEmail(email).isPresent()){
            throw new RuntimeException("email already exits");
        }
        User user=new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return new AuthResponse(true,null,"User Registered Successfully",email);
    }
    public AuthResponse login(AuthRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user=userRepository.findByEmail(request.getEmail()).orElseThrow(()->new RuntimeException("User Not Found"));
        String token=jwtService.generateToken(request.getEmail());
        return new AuthResponse(
                true,
                token,
                "User login Successfully",
                user.getEmail()
        );

    }
    public UserResponse getCurrentUser(String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));
        return new UserResponse(true,user.getEmail());
    }
}
