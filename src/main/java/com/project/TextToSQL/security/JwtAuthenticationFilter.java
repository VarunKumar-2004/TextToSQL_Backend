package com.project.TextToSQL.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.project.TextToSQL.security.JwtService;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // We will define this in the SecurityConfig next
    public JwtAuthenticationFilter(JwtService jwtService,UserDetailsService userDetailsService){
        this.jwtService=jwtService;
        this.userDetailsService=userDetailsService;
    }
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        System.out.println("AUTH HEADER = " + authHeader);
        final String jwt;
        final String userEmail;

        // 1. Check if the token exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token? Pass it down the chain (Spring Security will block it later if the endpoint is private)
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the token string (remove "Bearer " from the header)
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractEmail(jwt);
        System.out.println("USER EMAIL FROM TOKEN = " + userEmail);

        // 3. If we found an email, and the user isn't already authenticated in this transaction
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Fetch the user details from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Ask JwtService: Is this token valid and not forged?
            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                System.out.println("JWT IS VALID");
                // 6. Token is good! Create an authentication passport for Spring
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Store the passport in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue to the requested controller endpoint
        filterChain.doFilter(request, response);
    }
}
