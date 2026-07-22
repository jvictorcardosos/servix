package br.com.servix.auth.controller;

import br.com.servix.auth.dto.AuthResponse;
import br.com.servix.auth.dto.LoginRequest;
import br.com.servix.auth.dto.LogoutRequest;
import br.com.servix.auth.dto.RefreshTokenRequest;
import br.com.servix.auth.dto.RegisterUserRequest;
import br.com.servix.auth.dto.UserResponse;
import br.com.servix.auth.service.AuthService;
import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> register(
            @Valid @RequestBody RegisterUserRequest request,
            HttpServletRequest servletRequest) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseFactory.success(HttpStatus.CREATED, "Usuário criado com sucesso", servletRequest.getRequestURI(), response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseFactory.success(HttpStatus.OK, "Login realizado com sucesso", servletRequest.getRequestURI(), response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponseFactory.success(HttpStatus.OK, "Token atualizado com sucesso", servletRequest.getRequestURI(), response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiSuccessResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest servletRequest) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponseFactory.success(HttpStatus.OK, "Logout realizado com sucesso", servletRequest.getRequestURI(), null));
    }
}
