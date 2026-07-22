package br.com.servix.auth.dto;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        UUID userId,
        UUID companyId,
        String accessToken,
        String refreshToken,
        Set<String> profiles) {}
