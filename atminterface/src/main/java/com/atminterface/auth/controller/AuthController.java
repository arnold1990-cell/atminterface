package com.atminterface.auth.controller;

import com.atminterface.auth.dto.LoginRequest;
import com.atminterface.auth.dto.LoginResponse;
import com.atminterface.auth.dto.LogoutRequest;
import com.atminterface.auth.service.AuthService;
import com.atminterface.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful.", authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.sessionToken());
        return ApiResponse.success("Logged out successfully.", null);
    }
}
