package br.com.servix.schedule.repository;

import br.com.servix.schedule.domain.Employee;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> belongsToCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<Employee> nameContains(String value) {
        return likeIgnoreCase("name", value);
    }

    public static Specification<Employee> emailContains(String value) {
        return likeIgnoreCase("email", value);
    }

    public static Specification<Employee> phoneContains(String value) {
        return likeIgnoreCase("phone", value);
    }

    public static Specification<Employee> activeEquals(Boolean active) {
        return (root, query, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }

    public static Specification<Employee> genericFilter(String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            String like = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("phone")), like));
        };
    }

    private static Specification<Employee> likeIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.trim().toLowerCase(Locale.ROOT) + "%");
        };
    }
}
