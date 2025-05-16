package com.l1maor.vehicleworkshop.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsService userDetailsService, 
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         JwtAuthEntryPoint jwtAuthEntryPoint,
                         PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.passwordEncoder = passwordEncoder;
        logger.info("Security configuration initialized");
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        logger.debug("Configuring authentication manager with user details service");
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        logger.info("Authentication manager created successfully");
        return authManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.debug("Configuring security filter chain");
        http
            .csrf(csrf -> {
                csrf.disable();
                logger.debug("CSRF protection disabled");
            })
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                logger.debug("Session management set to STATELESS");
            })
            .authorizeHttpRequests(auth -> {
                logger.debug("Configuring endpoint access rules");
                auth
                // Static resources for frontend
                .requestMatchers(HttpMethod.GET, "/", "/*.html", "/*.js", "/*.css", "/*.ico", "/assets/**", "/manifest.json").permitAll()
                .requestMatchers(HttpMethod.GET, "/index.html").permitAll()
                // OpenAPI endpoints
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                // Allow authentication endpoints
                .requestMatchers("/api/auth/**").permitAll() 
                .requestMatchers("/api/roles/**").hasRole("ADMIN") // Restrict roles endpoint to ADMIN
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN") // Restrict user creation
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Vehicle operations - restrict create/update/delete to ADMIN only
                .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasRole("ADMIN")
                // Allow users to access their own profile
                .requestMatchers(HttpMethod.GET, "/api/users/profile").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated();
                logger.debug("HTTP authorization rules configured");
            })
            // Configure entry point to return 401 for unauthorized requests
            .exceptionHandling(exceptions -> {
                logger.debug("Configuring security exception handling");
                exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    logger.warn("Authentication failed for request to {}: {}", 
                            request.getRequestURI(), authException.getMessage());
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"message\":\"Unauthorized: " + authException.getMessage() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    logger.warn("Access denied for request to {}: {}", 
                            request.getRequestURI(), accessDeniedException.getMessage());
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"message\":\"Access denied: " + accessDeniedException.getMessage() + "\"}");
                });
                logger.debug("Security exception handling configured");
            })
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("Security filter chain configured successfully");
        return http.build();
    }
}
