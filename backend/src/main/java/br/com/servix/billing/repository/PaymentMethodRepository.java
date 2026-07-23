package br.com.servix.billing.repository;

import br.com.servix.billing.domain.PaymentMethod;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    @Query("""
            select pm from PaymentMethod pm
            where pm.active = true
              and (pm.companyId is null or pm.companyId = :companyId)
            order by pm.name asc
            """)
    List<PaymentMethod> findAvailable(@Param("companyId") UUID companyId);

    @Query("""
            select pm from PaymentMethod pm
            where pm.id = :id
              and pm.active = true
              and (pm.companyId is null or pm.companyId = :companyId)
            """)
    Optional<PaymentMethod> findActiveById(@Param("id") UUID id, @Param("companyId") UUID companyId);
}
