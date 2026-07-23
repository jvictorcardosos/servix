package br.com.servix.service.repository;

import br.com.servix.service.domain.ServiceOffering;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ServiceSpecifications {

    private ServiceSpecifications() {
    }

    public static Specification<ServiceOffering> belongsToCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<ServiceOffering> nameContains(String value) {
        return likeIgnoreCase("name", value);
    }

    public static Specification<ServiceOffering> activeEquals(Boolean active) {
        return (root, query, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }

    public static Specification<ServiceOffering> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }
            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("price"), minPrice, maxPrice);
            }
            if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<ServiceOffering> durationBetween(Integer minDuration, Integer maxDuration) {
        return (root, query, cb) -> {
            if (minDuration == null && maxDuration == null) {
                return cb.conjunction();
            }
            if (minDuration != null && maxDuration != null) {
                return cb.between(root.get("durationMinutes"), minDuration, maxDuration);
            }
            if (minDuration != null) {
                return cb.greaterThanOrEqualTo(root.get("durationMinutes"), minDuration);
            }
            return cb.lessThanOrEqualTo(root.get("durationMinutes"), maxDuration);
        };
    }

    public static Specification<ServiceOffering> genericFilter(String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            String like = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like));
        };
    }

    private static Specification<ServiceOffering> likeIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.trim().toLowerCase(Locale.ROOT) + "%");
        };
    }
}
