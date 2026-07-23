package br.com.servix.service.repository;

import br.com.servix.service.domain.ServiceOffering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceRepository extends JpaRepository<ServiceOffering, UUID>, JpaSpecificationExecutor<ServiceOffering> {

    Optional<ServiceOffering> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndName(UUID companyId, String name);

    boolean existsByCompanyIdAndNameAndIdNot(UUID companyId, String name, UUID id);

    List<ServiceOffering> findAllByCompanyIdAndIdIn(UUID companyId, Collection<UUID> ids);
}
