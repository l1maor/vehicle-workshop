package com.l1maor.vehicleworkshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.l1maor.vehicleworkshop.config.WebMvcTestConfig;
import com.l1maor.vehicleworkshop.dto.UserDto;
import com.l1maor.vehicleworkshop.entity.Role;
import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(WebMvcTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private UserService userService;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Setup roles
        adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(1L);

        userRole = new Role("ROLE_USER");
        userRole.setId(2L);

        // Setup users
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPasswordHash("hashedPassword");
        adminUser.setRoles(Set.of(adminRole));

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("user");
        regularUser.setPasswordHash("hashedPassword");
        regularUser.setRoles(Set.of(userRole));
    }

    @Test
    @DisplayName("GET /api/users - Get all users")
    void testGetAllUsers() throws Exception {
        // Given
        when(userService.findAllUsers()).thenReturn(Arrays.asList(adminUser, regularUser));

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("admin")))
                .andExpect(jsonPath("$[0].roles", hasSize(1)))
                .andExpect(jsonPath("$[0].roles[0]", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("user")))
                .andExpect(jsonPath("$[1].roles", hasSize(1)))
                .andExpect(jsonPath("$[1].roles[0]", is("ROLE_USER")))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Get user by ID")
    void testGetUserById() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/users/{id} - User not found")
    void testGetUserById_NotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users - Create user")
    void testCreateUser() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("newuser");
        userDto.setPassword("password123");
        userDto.setRoles(Set.of("ROLE_USER"));

        User newUser = new User();
        newUser.setId(3L);
        newUser.setUsername("newuser");
        newUser.setPasswordHash("hashedPassword");
        newUser.setRoles(Set.of(userRole));

        when(userService.createUser(any(), anyString(), anyList())).thenReturn(newUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_USER")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/users - Username already exists")
    void testCreateUser_UsernameExists() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("admin");
        userDto.setPassword("password123");
        userDto.setRoles(Set.of("ROLE_USER"));

        when(userService.createUser(any(), anyString(), anyList()))
                .thenThrow(new IllegalArgumentException("Username already exists: admin"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Update user")
    void testUpdateUser() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(2L);
        userDto.setUsername("updateduser");
        userDto.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

        User updatedUser = new User();
        updatedUser.setId(2L);
        updatedUser.setUsername("updateduser");
        updatedUser.setPasswordHash("hashedPassword");
        updatedUser.setRoles(Set.of(userRole, adminRole));

        when(userService.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userService.updateUser(eq(2L), any())).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_USER", "ROLE_ADMIN")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - User not found")
    void testUpdateUser_NotFound() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(999L);
        userDto.setUsername("updateduser");

        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Delete user")
    void testDeleteUser() throws Exception {
        // Given
        when(userService.deleteUser(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - User not found")
    void testDeleteUser_NotFound() throws Exception {
        // Given
        when(userService.deleteUser(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }
}
