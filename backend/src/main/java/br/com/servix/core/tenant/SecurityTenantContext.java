package br.com.servix.core.tenant;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityTenantContext {

    private SecurityTenantContext() {
    }

    public static Optional<UUID> currentTenantId() {
        return currentPrincipal().map(TenantPrincipal::getCompanyId);
    }

    public static Optional<UUID> currentUserId() {
        return currentPrincipal().map(TenantPrincipal::getUserId);
    }

    private static Optional<TenantPrincipal> currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof TenantPrincipal details)) {
            return Optional.empty();
        }
        return Optional.of(details);
    }
}
