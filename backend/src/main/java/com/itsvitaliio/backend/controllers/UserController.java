package com.itsvitaliio.backend.controllers;

import com.itsvitaliio.backend.dto.*;
import com.itsvitaliio.backend.services.UserService;
import com.itsvitaliio.backend.utilities.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notatky")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            return jwtUtil.extractUserId(jwt);
        }
        return null;
    }

    @PutMapping("/change-username")
    public ResponseEntity<?> changeUsername(HttpServletRequest request, @RequestBody ChangeUsernameRequest changeUsernameRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            userService.changeUsername(userId, changeUsernameRequest);
            return ResponseEntity.ok("Username changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/change-email")
    public ResponseEntity<?> changeEmail(HttpServletRequest request, @RequestBody ChangeEmailRequest changeEmailRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            userService.changeEmail(userId, changeEmailRequest);
            return ResponseEntity.ok("Email changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest changePasswordRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            String newToken = userService.changePassword(userId, changePasswordRequest);
            return ResponseEntity.ok("Password changed successfully. New Token: " + newToken);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            userService.deleteUserAccount(userId);
            return ResponseEntity.ok("Account deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
