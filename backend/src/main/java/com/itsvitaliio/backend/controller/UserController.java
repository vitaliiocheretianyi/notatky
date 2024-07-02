package com.itsvitaliio.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsvitaliio.backend.dto.UserDTO;
import com.itsvitaliio.backend.model.User;
import com.itsvitaliio.backend.model.LoginRequest;
import com.itsvitaliio.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/register")
    public void createUser(@RequestBody UserDTO userDTO) {
        try {
            String json = objectMapper.writeValueAsString(userDTO);
            logger.info("Received request body: {}", json);
            User user = new User();
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setPasswordHash(userDTO.getPassword());
            userService.createUser(user);
        } catch (Exception e) {
            logger.error("Error processing request: ", e);
            throw new RuntimeException("Invalid request payload", e);
        }
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest loginRequest) {
        try {
            String json = objectMapper.writeValueAsString(loginRequest);
            logger.info("Login request body: {}", json);
            return userService.login(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
        } catch (Exception e) {
            logger.error("Error processing request: ", e);
            throw new RuntimeException("Invalid request payload", e);
        }
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
