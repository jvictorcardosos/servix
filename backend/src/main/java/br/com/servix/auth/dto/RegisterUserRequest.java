package br.com.servix.auth.dto;

import br.com.servix.auth.domain.ProfileName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record RegisterUserRequest(
        @NotNull UUID companyId,
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String senha,
        Set<ProfileName> profiles) {}
