package com.l1maor.vehicleworkshop.controller;

import com.l1maor.vehicleworkshop.dto.UserDto;
import com.l1maor.vehicleworkshop.entity.Role;
import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Try to find current user by username
            Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
            
            if (currentUser.isPresent()) {
                return ResponseEntity.ok(convertToDto(currentUser.get()));
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        try {
            // Validate user data
            if (userDto.getUsername() == null || userDto.getUsername().isEmpty() ||
                userDto.getPassword() == null || userDto.getPassword().isEmpty() ||
                userDto.getRoles() == null || userDto.getRoles().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Username, password, and roles are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }
            
            User user = new User();
            user.setUsername(userDto.getUsername());

            User createdUser = userService.createUser(
                    user,
                    userDto.getPassword(),
                    new ArrayList<>(userDto.getRoles())
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(createdUser));
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Username already exists: " + userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        } catch (EntityNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Role does not exist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        try {
            User existingUser = userService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

            // Check if username is taken by another user
            Optional<User> userWithSameUsername = userService.findByUsername(userDto.getUsername());
            if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Username already exists: " + userDto.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }

            existingUser.setUsername(userDto.getUsername());
            
            // Update roles if provided
            if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
                try {
                    Set<Role> roles = new HashSet<>();
                    for (String roleName : userDto.getRoles()) {
                        roles.add(userService.findRoleByName(roleName)
                                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName)));
                    }
                    existingUser.setRoles(roles);
                } catch (EntityNotFoundException e) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(errorResponse);
                }
            }
            
            User updatedUser = userService.updateUser(id, existingUser);
            return ResponseEntity.ok(convertToDto(updatedUser));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());

        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        
        return dto;
    }
}
