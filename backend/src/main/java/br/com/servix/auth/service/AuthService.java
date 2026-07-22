package br.com.servix.auth.service;

import br.com.servix.auth.domain.Profile;
import br.com.servix.auth.domain.ProfileName;
import br.com.servix.auth.domain.RefreshToken;
import br.com.servix.auth.domain.User;
import br.com.servix.auth.domain.UserStatus;
import br.com.servix.auth.dto.AuthResponse;
import br.com.servix.auth.dto.LoginRequest;
import br.com.servix.auth.dto.RefreshTokenRequest;
import br.com.servix.auth.dto.RegisterUserRequest;
import br.com.servix.auth.dto.UserResponse;
import br.com.servix.auth.repository.ProfileRepository;
import br.com.servix.auth.repository.UserRepository;
import br.com.servix.company.domain.Company;
import br.com.servix.company.domain.CompanyStatus;
import br.com.servix.company.repository.CompanyRepository;
import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.core.validation.EmailNormalizer;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TenantContextService tenantContextService;

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        enforceRegistrationAccess(request.companyId());

        String normalizedEmail = EmailNormalizer.normalize(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empresa inativa");
        }

        User user = new User();
        user.setCompany(company);
        user.setNome(request.nome());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.senha()));
        user.setStatus(UserStatus.ACTIVE);
        user.setProfiles(resolveProfiles(request.profiles()));
        userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getCompany().getId(),
                user.getNome(),
                user.getEmail(),
                user.getStatus(),
                mapProfileNames(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.senha()));

        ServixUserDetails userDetails = (ServixUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário inválido"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validate(request.refreshToken());
        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE || user.getCompany().getStatus() != CompanyStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou empresa inativa");
        }

        refreshTokenService.revoke(refreshToken);
        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.issue(user);
        return buildAuthResponse(user, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        java.util.UUID userId = tenantContextService.getRequiredUserId();
        RefreshToken entity = refreshTokenService.findByRawToken(refreshToken);
        if (!entity.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não é permitido revogar token de outro usuário");
        }
        refreshTokenService.revoke(entity);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return new AuthResponse(
                user.getId(),
                user.getCompany().getId(),
                accessToken,
                refreshToken,
                mapProfileNames(user));
    }

    private Set<String> mapProfileNames(User user) {
        return user.getProfiles().stream()
                .map(profile -> profile.getName().name())
                .collect(Collectors.toSet());
    }

    private Set<Profile> resolveProfiles(Set<ProfileName> profileNames) {
        Set<ProfileName> requested = (profileNames == null || profileNames.isEmpty())
                ? Set.of(ProfileName.OPERADOR)
                : profileNames;

        Set<Profile> profiles = new HashSet<>();
        for (ProfileName name : requested) {
            Profile profile = profileRepository.findByName(name)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil inexistente: " + name));
            profiles.add(profile);
        }
        return profiles;
    }

    private void enforceRegistrationAccess(java.util.UUID companyId) {
        if (userRepository.count() == 0) {
            return;
        }

        tenantContextService.assertTenantAccess(companyId);
        if (!tenantContextService.hasRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas ADMIN pode cadastrar usuários");
        }
    }
}
