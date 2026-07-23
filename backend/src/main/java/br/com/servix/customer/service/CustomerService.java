package br.com.servix.customer.service;

import br.com.servix.core.config.CoreConstants;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.core.pagination.PageRequestParams;
import br.com.servix.core.pagination.PaginationUtils;
import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.dto.CustomerRequest;
import br.com.servix.customer.dto.CustomerResponse;
import br.com.servix.customer.dto.CustomerSearchRequest;
import br.com.servix.customer.dto.CustomerStatusUpdateRequest;
import br.com.servix.customer.mapper.CustomerMapper;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.customer.repository.CustomerSpecifications;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private static final String[] ALLOWED_SORT_FIELDS = {
            "nome", "cpfCnpj", "email", "telefone", "cidade", "estado", "ativo", "createdAt", "updatedAt"
    };

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final TenantContextService tenantContextService;

    public CustomerResponse create(CustomerRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        CustomerRequest normalized = normalize(request);
        Customer customer = customerMapper.toEntity(normalized);
        customer.setCompanyId(companyId);
        validateDuplicates(companyId, normalized.cpfCnpj(), normalized.email(), null);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> search(CustomerSearchRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        String sortBy = normalizeSortBy(request.sortBy());
        String direction = "DESC".equalsIgnoreCase(request.direction()) ? "DESC" : "ASC";
        Pageable pageable = PaginationUtils.toPageable(
                new PageRequestParams(
                        normalizePage(request.page()),
                        normalizeSize(request.size()),
                        sortBy,
                        direction,
                        request.effectiveFilter()));

        Specification<Customer> specification = CustomerSpecifications.belongsToCompany(companyId)
                .and(CustomerSpecifications.genericFilter(request.filter()))
                .and(CustomerSpecifications.nomeContains(request.nome()))
                .and(CustomerSpecifications.cpfCnpjContains(normalizeDocument(request.cpfCnpj())))
                .and(CustomerSpecifications.telefoneContains(normalizePhone(request.telefone())))
                .and(CustomerSpecifications.emailContains(normalizeEmail(request.email())))
                .and(CustomerSpecifications.statusEquals(request.ativo()));

        Page<CustomerResponse> page = customerRepository.findAll(specification, pageable)
                .map(customerMapper::toResponse);
        return PaginationUtils.fromPage(page, new PageRequestParams(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction,
                request.filter()));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        Customer customer = findCustomer(id);
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        CustomerRequest normalized = normalize(request);
        validateDuplicates(customer.getCompanyId(), normalized.cpfCnpj(), normalized.email(), customer.getId());
        customerMapper.apply(normalized, customer);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    public CustomerResponse updateStatus(UUID id, CustomerStatusUpdateRequest request) {
        Customer customer = findCustomer(id);
        customer.setAtivo(Boolean.TRUE.equals(request.ativo()));
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    public void delete(UUID id) {
        Customer customer = findCustomer(id);
        customerRepository.delete(customer);
    }

    private Customer findCustomer(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    private void validateDuplicates(UUID companyId, String cpfCnpj, String email, UUID ignoreId) {
        if (ignoreId == null) {
            if (customerRepository.existsByCompanyIdAndCpfCnpj(companyId, cpfCnpj)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF/CNPJ já cadastrado para a empresa");
            }
            if (customerRepository.existsByCompanyIdAndEmail(companyId, email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado para a empresa");
            }
            return;
        }

        if (customerRepository.existsByCompanyIdAndCpfCnpjAndIdNot(companyId, cpfCnpj, ignoreId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF/CNPJ já cadastrado para a empresa");
        }
        if (customerRepository.existsByCompanyIdAndEmailAndIdNot(companyId, email, ignoreId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado para a empresa");
        }
    }

    private CustomerRequest normalize(CustomerRequest request) {
        return new CustomerRequest(
                trim(request.nome()),
                normalizeDocument(request.cpfCnpj()),
                normalizeEmail(request.email()),
                normalizePhone(request.telefone()),
                normalizePhone(request.telefoneSecundario()),
                normalizeCep(request.cep()),
                trim(request.logradouro()),
                trim(request.numero()),
                trim(request.complemento()),
                trim(request.bairro()),
                trim(request.cidade()),
                trimUpper(request.estado()),
                trim(request.observacoes()));
    }

    private Integer normalizePage(Integer page) {
        return page == null || page < 0 ? CoreConstants.DEFAULT_PAGE : page;
    }

    private Integer normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return CoreConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, CoreConstants.MAX_PAGE_SIZE);
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }
        for (String allowed : ALLOWED_SORT_FIELDS) {
            if (allowed.equalsIgnoreCase(sortBy)) {
                return allowed;
            }
        }
        return "createdAt";
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimUpper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeDocument(String value) {
        return digitsOnly(value);
    }

    private String normalizePhone(String value) {
        return digitsOnly(value);
    }

    private String normalizeCep(String value) {
        return digitsOnly(value);
    }

    private String digitsOnly(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }
}
