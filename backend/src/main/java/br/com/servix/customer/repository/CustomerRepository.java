package br.com.servix.customer.repository;

import br.com.servix.customer.domain.Customer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndCpfCnpj(UUID companyId, String cpfCnpj);

    boolean existsByCompanyIdAndEmail(UUID companyId, String email);

    boolean existsByCompanyIdAndCpfCnpjAndIdNot(UUID companyId, String cpfCnpj, UUID id);

    boolean existsByCompanyIdAndEmailAndIdNot(UUID companyId, String email, UUID id);

    List<Customer> findAllByCompanyIdAndIdIn(UUID companyId, Collection<UUID> ids);
}
