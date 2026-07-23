package br.com.servix.core.tenant;

import java.util.UUID;

public interface TenantPrincipal {

    UUID getUserId();

    UUID getCompanyId();
}
