package br.com.servix.schedule.repository;

import br.com.servix.schedule.domain.Employee;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndEmail(UUID companyId, String email);

    boolean existsByCompanyIdAndEmailAndIdNot(UUID companyId, String email, UUID id);

    List<Employee> findAllByCompanyIdAndIdIn(UUID companyId, Collection<UUID> ids);
}
