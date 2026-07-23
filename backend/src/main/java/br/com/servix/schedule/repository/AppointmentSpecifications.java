package br.com.servix.schedule.repository;

import br.com.servix.schedule.domain.Appointment;
import br.com.servix.schedule.domain.AppointmentStatus;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AppointmentSpecifications {

    private AppointmentSpecifications() {
    }

    public static Specification<Appointment> belongsToCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<Appointment> employeeEquals(UUID employeeId) {
        return (root, query, cb) -> employeeId == null ? cb.conjunction() : cb.equal(root.get("employeeId"), employeeId);
    }

    public static Specification<Appointment> customerEquals(UUID customerId) {
        return (root, query, cb) -> customerId == null ? cb.conjunction() : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<Appointment> serviceEquals(UUID serviceId) {
        return (root, query, cb) -> serviceId == null ? cb.conjunction() : cb.equal(root.get("serviceId"), serviceId);
    }

    public static Specification<Appointment> statusEquals(AppointmentStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Appointment> dateFrom(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("appointmentDate"), date);
    }

    public static Specification<Appointment> dateTo(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("appointmentDate"), date);
    }

    public static Specification<Appointment> genericFilter(String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            String like = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("notes")), like);
        };
    }
}
