package br.com.servix.auth.dto;

import br.com.servix.auth.domain.UserStatus;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID companyId,
        String nome,
        String email,
        UserStatus status,
        Set<String> profiles) {}
