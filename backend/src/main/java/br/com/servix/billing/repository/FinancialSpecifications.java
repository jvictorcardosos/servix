package br.com.servix.billing.repository;

import br.com.servix.billing.domain.FinancialStatus;
import br.com.servix.billing.domain.FinancialTransaction;
import br.com.servix.billing.domain.TransactionType;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class FinancialSpecifications {

    private FinancialSpecifications() {
    }

    public static Specification<FinancialTransaction> belongsToCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<FinancialTransaction> serviceOrderEquals(UUID serviceOrderId) {
        return (root, query, cb) -> serviceOrderId == null ? cb.conjunction() : cb.equal(root.get("serviceOrderId"), serviceOrderId);
    }

    public static Specification<FinancialTransaction> customerEquals(UUID customerId) {
        return (root, query, cb) -> customerId == null ? cb.conjunction() : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<FinancialTransaction> professionalEquals(UUID professionalId) {
        return (root, query, cb) -> professionalId == null ? cb.conjunction() : cb.equal(root.get("professionalId"), professionalId);
    }

    public static Specification<FinancialTransaction> serviceEquals(UUID serviceId) {
        return (root, query, cb) -> serviceId == null ? cb.conjunction() : cb.equal(root.get("serviceId"), serviceId);
    }

    public static Specification<FinancialTransaction> paymentMethodEquals(UUID paymentMethodId) {
        return (root, query, cb) -> paymentMethodId == null ? cb.conjunction() : cb.equal(root.get("paymentMethodId"), paymentMethodId);
    }

    public static Specification<FinancialTransaction> statusEquals(FinancialStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<FinancialTransaction> transactionTypeEquals(TransactionType transactionType) {
        return (root, query, cb) -> transactionType == null ? cb.conjunction() : cb.equal(root.get("transactionType"), transactionType);
    }

    public static Specification<FinancialTransaction> dueDateFrom(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("dueDate"), date);
    }

    public static Specification<FinancialTransaction> dueDateTo(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("dueDate"), date);
    }

    public static Specification<FinancialTransaction> genericFilter(String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            String like = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("externalReference")), like));
        };
    }
}
