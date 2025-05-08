package com.l1maor.vehicleworkshop.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuthEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthEventListener.class);
    
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        String username;
        
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        
        logger.info("Successful login by user: {} at {}", username, LocalDateTime.now());
    }
    
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        logger.warn("Failed login attempt for user: {} at {}, reason: {}", 
                username, LocalDateTime.now(), event.getException().getMessage());
    }
}
