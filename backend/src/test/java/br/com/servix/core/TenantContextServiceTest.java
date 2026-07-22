package br.com.servix.core;

import br.com.servix.auth.domain.Profile;
import br.com.servix.auth.domain.ProfileName;
import br.com.servix.auth.domain.User;
import br.com.servix.auth.domain.UserStatus;
import br.com.servix.auth.service.ServixUserDetails;
import br.com.servix.company.domain.Company;
import br.com.servix.company.domain.CompanyStatus;
import br.com.servix.core.tenant.TenantContextService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantContextServiceTest {

    private final TenantContextService tenantContextService = new TenantContextService();

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveTenantAndUserFromSecurityContext() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(buildUserDetails(userId, companyId), null, Set.of()));

        assertThat(tenantContextService.getRequiredTenantId()).isEqualTo(companyId);
        assertThat(tenantContextService.getRequiredUserId()).isEqualTo(userId);
    }

    @Test
    void shouldThrowWhenTenantContextIsMissing() {
        assertThatThrownBy(() -> tenantContextService.getRequiredTenantId())
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldDetectRoles() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ServixUserDetails userDetails = buildUserDetails(userId, companyId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        assertThat(tenantContextService.hasRole("ADMIN")).isTrue();
        assertThat(tenantContextService.hasRole("GESTOR")).isFalse();
    }

    private ServixUserDetails buildUserDetails(UUID userId, UUID companyId) {
        Company company = new Company();
        company.setId(companyId);
        company.setNome("Empresa");
        company.setDocumento("123");
        company.setEmail("empresa@servix.com");
        company.setStatus(CompanyStatus.ACTIVE);

        Profile profile = new Profile();
        profile.setId(1L);
        profile.setName(ProfileName.ADMIN);

        User user = new User();
        user.setId(userId);
        user.setCompany(company);
        user.setNome("Admin");
        user.setEmail("admin@servix.com");
        user.setPasswordHash("hash");
        user.setStatus(UserStatus.ACTIVE);
        user.setProfiles(Set.of(profile));
        return new ServixUserDetails(user);
    }
}
