package com.comeeatme.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/docs/auth")
@RestController
public class AuthDocsController {

    @GetMapping("/login-response")
    public ResponseEntity<LoginResponse> loginResponse() {
        LoginResponse loginResponse = new LoginResponse(
                "<Access token>", "<Refresh token>");
        return ResponseEntity.ok(loginResponse);
    }
}
