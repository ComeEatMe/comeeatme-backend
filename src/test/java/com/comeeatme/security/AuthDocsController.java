package com.comeeatme.security;

import com.comeeatme.security.response.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/docs/auth")
@RestController
public class AuthDocsController {

    @GetMapping("/login-response")
    public ResponseEntity<LoginResponse> loginResponse() {
        LoginResponse loginResponse = LoginResponse.builder()
                .memberId(1L)
                .accessToken("<Access token>")
                .refreshToken("<Refresh token>")
                .build();
        return ResponseEntity.ok(loginResponse);
    }
}
