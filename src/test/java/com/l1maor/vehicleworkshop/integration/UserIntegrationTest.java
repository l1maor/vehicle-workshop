package com.l1maor.vehicleworkshop.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.l1maor.vehicleworkshop.data.TestDataGenerator;
import com.l1maor.vehicleworkshop.dto.UserDto;
import com.l1maor.vehicleworkshop.entity.Role;
import com.l1maor.vehicleworkshop.entity.User;
import com.l1maor.vehicleworkshop.repository.RoleRepository;
import com.l1maor.vehicleworkshop.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestDataGenerator dataGenerator;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clear database
        dataGenerator.clearDatabase();

        // Create roles
        adminRole = dataGenerator.createRoleIfNotExists("ROLE_ADMIN");
        userRole = dataGenerator.createRoleIfNotExists("ROLE_USER");

        // Create users
        adminUser = dataGenerator.createUser("admin", "admin123", "ROLE_ADMIN");
        regularUser = dataGenerator.createUser("user", "user123", "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        dataGenerator.clearDatabase();
    }

    @Test
    @DisplayName("GET /api/users - Get all users")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("admin", "user")))
                .andExpect(jsonPath("$[*].roles[0]", containsInAnyOrder("ROLE_ADMIN", "ROLE_USER")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Get user by ID")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetUserById() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/" + adminUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(adminUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/users - Create user")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUser() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("newuser");
        userDto.setPassword("password123");
        userDto.setRoles(Set.of("ROLE_USER", "ROLE_MECHANIC"));

        // Create mechanic role first
        dataGenerator.createRoleIfNotExists("ROLE_MECHANIC");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_USER", "ROLE_MECHANIC")))
                .andExpect(jsonPath("$.password").doesNotExist());

        // Verify user was added to database
        Optional<User> newUser = userRepository.findByUsername("newuser");
        assertTrue(newUser.isPresent());
        assertEquals(2, newUser.get().getRoles().size());
    }

    @Test
    @DisplayName("POST /api/users - Username already exists")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUser_UsernameExists() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("admin");  // Already exists
        userDto.setPassword("password123");
        userDto.setRoles(Set.of("ROLE_USER"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Update user")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(regularUser.getId());
        userDto.setUsername("updateduser");
        userDto.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

        // When & Then
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(regularUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_USER", "ROLE_ADMIN")));

        // Verify user was updated in database
        Optional<User> updatedUser = userRepository.findById(regularUser.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals("updateduser", updatedUser.get().getUsername());
        assertEquals(2, updatedUser.get().getRoles().size());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Delete user")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/" + regularUser.getId()))
                .andExpect(status().isNoContent());

        // Verify user was deleted from database
        assertFalse(userRepository.existsById(regularUser.getId()));
    }

    @Test
    @DisplayName("Authorization - Unauthorized access")
    void testUnauthorizedAccess() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authorization - Access with insufficient privileges")
    @WithMockUser(username = "user", roles = {"USER"})
    void testInsufficientPrivileges() throws Exception {
        // When & Then - User with USER role attempting admin operations
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Error handling - User not found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"roles\":[\"ROLE_USER\"]}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Validation - Invalid user data")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testInvalidUserData() throws Exception {
        // Given - Empty username
        UserDto userDto = new UserDto();
        userDto.setUsername("");
        userDto.setPassword("password123");
        userDto.setRoles(Set.of("ROLE_USER"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        // Given - Missing password
        userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setRoles(Set.of("ROLE_USER"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        // Given - Empty roles
        userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setPassword("password123");
        userDto.setRoles(new HashSet<>());

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin operations with admin role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminOperationsWithAdminRole() throws Exception {
        // When & Then - Admin accessing admin endpoints
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/" + regularUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Create user with non-existent role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUserWithNonExistentRole() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("roleuser");
        userDto.setPassword("password123");
        userDto.setRoles(Set.of("ROLE_NONEXISTENT"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not exist")));
    }

    @Test
    @DisplayName("Update user with duplicate username")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserWithDuplicateUsername() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(regularUser.getId());
        userDto.setUsername("admin"); // Duplicate username
        userDto.setRoles(Set.of("ROLE_USER"));

        // When & Then
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("User can access their own profile")
    @WithMockUser(username = "user", roles = {"USER"})
    void testUserCanAccessOwnProfile() throws Exception {
        // When & Then - User accessing their own profile
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("user")));
    }

    @Test
    @DisplayName("Create and delete roles")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateAndDeleteRoles() throws Exception {
        // Given - Create a new role
        String roleName = "ROLE_TEST";
        
        // When & Then - Create role
        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + roleName + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(roleName)));

        // Verify role was created
        assertTrue(roleRepository.findByName(roleName).isPresent());

        // Get the role ID
        Role role = roleRepository.findByName(roleName).get();

        // When & Then - Delete role
        mockMvc.perform(delete("/api/roles/" + role.getId()))
                .andExpect(status().isNoContent());

        // Verify role was deleted
        assertFalse(roleRepository.findByName(roleName).isPresent());
    }
}
