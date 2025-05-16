package com.l1maor.vehicleworkshop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    
    public JwtAuthenticationFilter(UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain chain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.debug("Processing request: {} {}", method, requestURI);
        
        try {
            final String authorizationHeader = request.getHeader("Authorization");
            
            String username = null;
            String jwt = null;
            
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                logger.debug("JWT token found in request");
                try {
                    username = jwtTokenUtil.getUsernameFromToken(jwt);
                    logger.debug("Username extracted from JWT token: {}", username);
                } catch (Exception e) {
                    logger.warn("JWT token validation error for request {} {}: {}", method, requestURI, e.getMessage());
                    logger.debug("JWT token validation error details", e);
                }
            } else {
                logger.debug("No JWT token found in request to {}", requestURI);
            }
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Loading UserDetails for authenticated user: {}", username);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                    logger.debug("JWT token validated successfully for user: {}", username);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set in SecurityContext for user: {} with authorities: {}", 
                               username, userDetails.getAuthorities());
                } else {
                    logger.warn("JWT token validation failed for user: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication for request {} {}: {}", method, requestURI, e.getMessage());
            logger.debug("Authentication error details", e);
        }
        
        logger.debug("Continuing filter chain for request: {} {}", method, requestURI);
        chain.doFilter(request, response);
    }
}
