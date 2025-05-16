package com.l1maor.vehicleworkshop.security;

import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger.debug("CustomUserDetailsService initialized");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username: {}", username);
        
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("Failed login attempt - user not found: {}", username);
                        return new UsernameNotFoundException("User not found with username: " + username);
                    });
            
            logger.debug("User found: {} with role: {}", user.getUsername(), user.getRoleType());
            
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPasswordHash(),
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRoleType().name()))
            );
            
            logger.debug("User details loaded successfully for: {}", username);
            return userDetails;
        } catch (Exception e) {
            if (!(e instanceof UsernameNotFoundException)) {
                logger.error("Unexpected error loading user: {}", username, e);
            }
            throw e;
        }
    }


}
