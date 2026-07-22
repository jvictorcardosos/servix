package br.com.servix.core.tenant;

import br.com.servix.auth.service.ServixUserDetails;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityTenantContext {

    private SecurityTenantContext() {
    }

    public static Optional<UUID> currentTenantId() {
        return currentPrincipal().map(ServixUserDetails::getCompanyId);
    }

    public static Optional<UUID> currentUserId() {
        return currentPrincipal().map(ServixUserDetails::getUserId);
    }

    private static Optional<ServixUserDetails> currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ServixUserDetails details)) {
            return Optional.empty();
        }
        return Optional.of(details);
    }
}
