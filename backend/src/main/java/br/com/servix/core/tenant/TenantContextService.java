package br.com.servix.core.tenant;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TenantContextService {

    public UUID getRequiredTenantId() {
        return SecurityTenantContext.currentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant não encontrado no contexto"));
    }

    public UUID getRequiredUserId() {
        return SecurityTenantContext.currentUserId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não encontrado no contexto"));
    }

    public boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String expected = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(expected));
    }

    public void assertTenantAccess(UUID tenantId) {
        if (!getRequiredTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso a tenant não permitido");
        }
    }
}
