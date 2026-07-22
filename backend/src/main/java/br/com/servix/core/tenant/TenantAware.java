package br.com.servix.core.tenant;

import java.util.UUID;

public interface TenantAware {

    UUID getCompanyId();

    void setCompanyId(UUID companyId);
}
