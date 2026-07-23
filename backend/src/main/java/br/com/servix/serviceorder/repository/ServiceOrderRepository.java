package br.com.servix.serviceorder.repository;

import br.com.servix.serviceorder.domain.ServiceOrder;
import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID>, JpaSpecificationExecutor<ServiceOrder> {

    Optional<ServiceOrder> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<ServiceOrder> findByAppointmentIdAndCompanyId(UUID appointmentId, UUID companyId);

    boolean existsByCompanyIdAndAppointmentId(UUID companyId, UUID appointmentId);

    boolean existsByCompanyIdAndProfessionalIdAndStatusAndIdNot(
            UUID companyId,
            UUID professionalId,
            ServiceOrderStatus status,
            UUID id);

    List<ServiceOrder> findAllByCompanyIdAndIdIn(UUID companyId, Collection<UUID> ids);
}
