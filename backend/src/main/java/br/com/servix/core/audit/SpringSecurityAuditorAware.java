package br.com.servix.core.audit;

import br.com.servix.core.tenant.SecurityTenantContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return SecurityTenantContext.currentUserId();
    }
}
