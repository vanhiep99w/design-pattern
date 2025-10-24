package com.designpatterns.showcase.mvc.controller;

import com.designpatterns.showcase.common.domain.User;
import com.designpatterns.showcase.common.domain.UserRole;
import com.designpatterns.showcase.common.repository.UserRepository;
import com.designpatterns.showcase.mvc.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("User Controller Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .active(true)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("GET /api/users - should return all users")
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[0].email", is("test@example.com")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - should return user by id")
    void getUserById_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - should return 404 for non-existent user")
    void getUserById_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("GET /api/users/role/{role} - should return users by role")
    void getUsersByRole_ShouldReturnFilteredUsers() throws Exception {
        mockMvc.perform(get("/api/users/role/{role}", UserRole.USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role", is("USER")));
    }

    @Test
    @DisplayName("GET /api/users/active - should return active users")
    void getActiveUsers_ShouldReturnOnlyActiveUsers() throws Exception {
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    @DisplayName("POST /api/users - should create new user")
    void createUser_WithValidData_ShouldCreateUser() throws Exception {
        UserDTO newUser = UserDTO.builder()
                .username("newuser")
                .email("newuser@example.com")
                .firstName("New")
                .lastName("User")
                .role(UserRole.USER)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("newuser@example.com")));
    }

    @Test
    @DisplayName("POST /api/users - should return 400 for invalid email")
    void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        UserDTO invalidUser = UserDTO.builder()
                .username("newuser")
                .email("invalid-email")
                .firstName("New")
                .lastName("User")
                .role(UserRole.USER)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/users - should return 400 for duplicate username")
    void createUser_WithDuplicateUsername_ShouldReturn400() throws Exception {
        UserDTO duplicateUser = UserDTO.builder()
                .username("testuser")
                .email("another@example.com")
                .firstName("Another")
                .lastName("User")
                .role(UserRole.USER)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Username already exists")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - should update existing user")
    void updateUser_WithValidData_ShouldUpdateUser() throws Exception {
        UserDTO updateDTO = UserDTO.builder()
                .username("testuser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - should return 404 for non-existent user")
    void updateUser_WithInvalidId_ShouldReturn404() throws Exception {
        UserDTO updateDTO = UserDTO.builder()
                .username("testuser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .role(UserRole.USER)
                .build();

        mockMvc.perform(put("/api/users/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - should delete user")
    void deleteUser_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - should return 404 for non-existent user")
    void deleteUser_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

}
