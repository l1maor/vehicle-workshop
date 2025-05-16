package com.l1maor.vehicleworkshop.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException, ServletException {
        
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        logger.warn("Unauthorized access attempt: {} {} - {}", method, requestUri, authException.getMessage());
        
        // Special handling for /api/auth/validate which requires POST method
        if (requestUri.equals("/api/auth/validate") && !method.equals("POST")) {
            logger.warn("Invalid request method for /api/auth/validate. Using {} but POST is required", method);
        }
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        String message = authException.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "Unauthorized access";
        }
        
        logger.debug("Returning unauthorized response with message: {}", message);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
