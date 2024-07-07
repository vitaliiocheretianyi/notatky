package com.itsvitaliio.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/notatky/hello")
    public String hello() {
        return "Hello, secured world!";
    }
}
