package br.com.servix.billing.repository;

import br.com.servix.billing.domain.FinancialStatus;
import br.com.servix.billing.domain.FinancialTransaction;
import br.com.servix.billing.domain.TransactionType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID>, JpaSpecificationExecutor<FinancialTransaction> {

    Optional<FinancialTransaction> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<FinancialTransaction> findByServiceOrderIdAndCompanyId(UUID serviceOrderId, UUID companyId);

    boolean existsByServiceOrderIdAndCompanyId(UUID serviceOrderId, UUID companyId);

    List<FinancialTransaction> findAllByCompanyIdAndIdIn(UUID companyId, Collection<UUID> ids);

    List<FinancialTransaction> findAllByCompanyIdAndStatus(UUID companyId, FinancialStatus status);

    List<FinancialTransaction> findAllByCompanyIdAndDueDateBetweenAndStatusIn(
            UUID companyId,
            LocalDate from,
            LocalDate to,
            Collection<FinancialStatus> statuses);

    List<FinancialTransaction> findAllByCompanyIdAndDueDateBeforeAndStatusIn(
            UUID companyId,
            LocalDate date,
            Collection<FinancialStatus> statuses);

    List<FinancialTransaction> findAllByCompanyIdAndTransactionType(UUID companyId, TransactionType transactionType);
}
