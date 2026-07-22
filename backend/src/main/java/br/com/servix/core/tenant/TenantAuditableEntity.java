package br.com.servix.core.tenant;

import br.com.servix.core.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class TenantAuditableEntity extends AuditableEntity implements TenantAware {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @PrePersist
    void fillTenantOnCreate() {
        if (companyId == null) {
            SecurityTenantContext.currentTenantId().ifPresent(this::setCompanyId);
        }
    }
}
