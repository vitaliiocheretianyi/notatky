package com.itsvitaliio.backend.controllers;

import com.itsvitaliio.backend.dto.LoginRequest;
import com.itsvitaliio.backend.dto.RegisterRequest;
import com.itsvitaliio.backend.dto.ServiceResponse;
import com.itsvitaliio.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        ServiceResponse<String> response = userService.register(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response.getMessage());
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/login/email")
    public ResponseEntity<?> loginWithEmail(@RequestBody LoginRequest request) {
        ServiceResponse<String> response = userService.loginWithEmail(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response.getMessage());
        }
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/login/username")
    public ResponseEntity<?> loginWithUsername(@RequestBody LoginRequest request) {
        ServiceResponse<String> response = userService.loginWithUsername(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response.getMessage());
        }
        return ResponseEntity.ok(response.getData());
    }
}
