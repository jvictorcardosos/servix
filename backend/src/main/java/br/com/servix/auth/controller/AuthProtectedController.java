package br.com.servix.auth.controller;

import br.com.servix.auth.service.ServixUserDetails;
import br.com.servix.core.api.ApiResponseFactory;
import br.com.servix.core.api.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthProtectedController {

    @GetMapping("/me")
    public ApiSuccessResponse<Map<String, String>> me(Authentication authentication, HttpServletRequest request) {
        ServixUserDetails userDetails = (ServixUserDetails) authentication.getPrincipal();
        Map<String, String> data = Map.of(
                "userId", userDetails.getUserId().toString(),
                "companyId", userDetails.getCompanyId().toString(),
                "email", userDetails.getUsername());
        return ApiResponseFactory.success(HttpStatus.OK, "Usuário autenticado", request.getRequestURI(), data);
    }

    @GetMapping("/admin")
    public ApiSuccessResponse<Map<String, String>> admin(HttpServletRequest request) {
        return ApiResponseFactory.success(HttpStatus.OK, "Acesso de administrador concedido", request.getRequestURI(), Map.of("status", "ok"));
    }
}
