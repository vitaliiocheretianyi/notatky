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
        System.out.println("Received Register Request: " + request);
        ServiceResponse<String> response = userService.register(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("Received Login Request: " + request);
        ServiceResponse<String> response;
        if (request.getEmail() != null) {
            response = userService.loginWithEmail(request);
        } else {
            response = userService.loginWithUsername(request);
        }

        if (!response.isSuccess()) {
            System.out.println(response);
            System.out.println("FAILURE TO LOG IN");
            return ResponseEntity.badRequest().body(response);
        }
        System.out.println("SUCCESSFULLY LOGGED IN:" + response);
        return ResponseEntity.ok(response);
    }
}
