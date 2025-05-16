package com.l1maor.vehicleworkshop.controller;

import com.l1maor.vehicleworkshop.dto.UserDto;
import com.l1maor.vehicleworkshop.entity.RoleType;
import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.service.DtoMapperService;
import com.l1maor.vehicleworkshop.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    private final DtoMapperService dtoMapperService;

    public UserController(UserService userService, DtoMapperService dtoMapperService) {
        this.userService = userService;
        this.dtoMapperService = dtoMapperService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userService.findAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(dtoMapperService::mapToDto)
                .collect(Collectors.toList());
        logger.debug("Retrieved {} users", users.size());
        return ResponseEntity.ok(userDtos);
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<UserDto>> getAllUsersPaginated(Pageable pageable) {
        logger.info("Fetching paginated users: page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<User> userPage = userService.findAllUsersPaginated(pageable);
        Page<UserDto> dtoPage = userPage.map(dtoMapperService::mapToDto);
        logger.debug("Retrieved {} users (page {} of {})", 
                userPage.getNumberOfElements(), pageable.getPageNumber(), userPage.getTotalPages());
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID: {}", id);
        return userService.findById(id)
                .map(user -> {
                    logger.debug("Found user: {} (ID: {})", user.getUsername(), id);
                    return ResponseEntity.ok(dtoMapperService.mapToDto(user));
                })
                .orElseGet(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        logger.info("Getting current user profile");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.debug("Authenticated user: {}", userDetails.getUsername());
            
            Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
            
            if (currentUser.isPresent()) {
                logger.debug("Found profile for user: {}", userDetails.getUsername());
                return ResponseEntity.ok(dtoMapperService.mapToDto(currentUser.get()));
            } else {
                logger.warn("User authenticated but profile not found: {}", userDetails.getUsername());
            }
        } else {
            logger.warn("Failed to retrieve user profile - no authentication context");
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        logger.info("Creating new user with username: {}", userDto.getUsername());
        try {
            // Validate user data
            if (userDto.getUsername() == null || userDto.getUsername().isEmpty() ||
                userDto.getPassword() == null || userDto.getPassword().isEmpty() ||
                userDto.getRoleType() == null || userDto.getRoleType().isEmpty()) {
                logger.warn("Invalid user data provided - missing required fields");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Username, password, and role type are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }
            
            User user = new User();
            user.setUsername(userDto.getUsername());

            RoleType roleType = userService.findRoleByName(userDto.getRoleType());
            user.setRoleType(roleType);
            logger.debug("Retrieved role type: {} for user", roleType);

            List<String> roleNames = new ArrayList<>();
            roleNames.add(userDto.getRoleType());
            
            User createdUser = userService.createUser(
                    user,
                    userDto.getPassword(),
                    roleNames
            );
            
            logger.info("User created successfully: {} (ID: {})", createdUser.getUsername(), createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapperService.mapToDto(createdUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create user - username already exists: {}", userDto.getUsername());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Username already exists: " + userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        } catch (EntityNotFoundException e) {
            logger.warn("Failed to create user - invalid role: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Role does not exist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        logger.info("Updating user ID: {} with username: {}", id, userDto.getUsername());
        try {
            User existingUser = userService.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Update failed - user not found with ID: {}", id);
                        return new EntityNotFoundException("User not found with id: " + id);
                    });
            logger.debug("Found existing user: {} (ID: {})", existingUser.getUsername(), id);

            // Check if username is taken by another user
            Optional<User> userWithSameUsername = userService.findByUsername(userDto.getUsername());
            if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
                logger.warn("Username already in use by another user: {}", userDto.getUsername());
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Username already exists: " + userDto.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }

            existingUser.setUsername(userDto.getUsername());

            if (userDto.getRoleType() != null && !userDto.getRoleType().isEmpty()) {
                try {
                    RoleType roleType = userService.findRoleByName(userDto.getRoleType());
                    logger.debug("Setting user role to: {}", roleType);
                    existingUser.setRoleType(roleType);
                } catch (EntityNotFoundException e) {
                    logger.warn("Invalid role specified during update: {}", userDto.getRoleType());
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorResponse);
                }
            }
            
            User updatedUser = userService.updateUser(id, existingUser);
            logger.info("User updated successfully: {} (ID: {})", updatedUser.getUsername(), id);
            return ResponseEntity.ok(dtoMapperService.mapToDto(updatedUser));
        } catch (EntityNotFoundException e) {
            logger.warn("Update failed - user not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        if (userService.deleteUser(id)) {
            logger.info("User deleted successfully: ID {}", id);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Delete failed - user not found with ID: {}", id);
        return ResponseEntity.notFound().build();
    }
}
