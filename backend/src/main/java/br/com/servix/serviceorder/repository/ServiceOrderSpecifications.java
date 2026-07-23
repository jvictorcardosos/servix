package br.com.servix.serviceorder.repository;

import br.com.servix.serviceorder.domain.ServiceOrder;
import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ServiceOrderSpecifications {

    private ServiceOrderSpecifications() {
    }

    public static Specification<ServiceOrder> belongsToCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<ServiceOrder> appointmentEquals(UUID appointmentId) {
        return (root, query, cb) -> appointmentId == null ? cb.conjunction() : cb.equal(root.get("appointmentId"), appointmentId);
    }

    public static Specification<ServiceOrder> customerEquals(UUID customerId) {
        return (root, query, cb) -> customerId == null ? cb.conjunction() : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<ServiceOrder> professionalEquals(UUID professionalId) {
        return (root, query, cb) -> professionalId == null ? cb.conjunction() : cb.equal(root.get("professionalId"), professionalId);
    }

    public static Specification<ServiceOrder> serviceEquals(UUID serviceId) {
        return (root, query, cb) -> serviceId == null ? cb.conjunction() : cb.equal(root.get("serviceId"), serviceId);
    }

    public static Specification<ServiceOrder> statusEquals(ServiceOrderStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<ServiceOrder> scheduledStartFrom(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("scheduledStart"), date.atStartOfDay());
    }

    public static Specification<ServiceOrder> scheduledStartTo(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("scheduledStart"), date.atTime(LocalTime.MAX));
    }

    public static Specification<ServiceOrder> genericFilter(String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            String like = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("observations")), like);
        };
    }
}
