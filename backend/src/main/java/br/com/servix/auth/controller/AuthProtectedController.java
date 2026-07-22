package br.com.servix.auth.controller;

import br.com.servix.auth.service.ServixUserDetails;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthProtectedController {

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        ServixUserDetails userDetails = (ServixUserDetails) authentication.getPrincipal();
        return Map.of(
                "userId", userDetails.getUserId().toString(),
                "companyId", userDetails.getCompanyId().toString(),
                "email", userDetails.getUsername());
    }

    @GetMapping("/admin")
    public Map<String, String> admin() {
        return Map.of("status", "ok");
    }
}
