package br.com.servix.customer.repository;

import br.com.servix.customer.domain.Customer;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CustomerSpecifications {

    private CustomerSpecifications() {
    }

    public static Specification<Customer> belongsToCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<Customer> nomeContains(String value) {
        return likeIgnoreCase("nome", value);
    }

    public static Specification<Customer> cpfCnpjContains(String value) {
        return likeIgnoreCase("cpfCnpj", value);
    }

    public static Specification<Customer> telefoneContains(String value) {
        return likeIgnoreCase("telefone", value);
    }

    public static Specification<Customer> emailContains(String value) {
        return likeIgnoreCase("email", value);
    }

    public static Specification<Customer> statusEquals(Boolean ativo) {
        return (root, query, cb) -> ativo == null ? cb.conjunction() : cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<Customer> genericFilter(String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            String like = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nome")), like),
                    cb.like(cb.lower(root.get("cpfCnpj")), like),
                    cb.like(cb.lower(root.get("telefone")), like),
                    cb.like(cb.lower(root.get("telefoneSecundario")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("cidade")), like));
        };
    }

    private static Specification<Customer> likeIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(value)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.trim().toLowerCase(Locale.ROOT) + "%");
        };
    }
}
