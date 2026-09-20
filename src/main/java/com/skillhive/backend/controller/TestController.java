package com.skillhive.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "JWT authentication successful!";
    }

    @DeleteMapping("/delete-test")
    public String deleteTest(Authentication authentication) {
        return "DELETE authentication successful! User: "
                + authentication.getName();
    }
}