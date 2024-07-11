package com.itsvitaliio.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itsvitaliio.backend.dto.ChangeEmailRequest;
import com.itsvitaliio.backend.dto.ChangePasswordRequest;
import com.itsvitaliio.backend.dto.ChangeUsernameRequest;

@RestController
@RequestMapping("/notatky/user")
public class UserController {

    @PutMapping("/change-username")
    public ResponseEntity<?> changeUsername(@RequestBody ChangeUsernameRequest request) {
        // Change username logic
        return null;
    }

    @PutMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody ChangeEmailRequest request) {
        // Change email logic
        return null;
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        // Change password logic
        return null;
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestParam Long userId) {
        // Delete account logic
        return null;
    }
}
