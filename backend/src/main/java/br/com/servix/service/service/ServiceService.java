package br.com.servix.service.service;

import br.com.servix.core.config.CoreConstants;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.core.pagination.PageRequestParams;
import br.com.servix.core.pagination.PaginationUtils;
import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.dto.ServiceRequest;
import br.com.servix.service.dto.ServiceResponse;
import br.com.servix.service.dto.ServiceSearchRequest;
import br.com.servix.service.dto.ServiceStatusUpdateRequest;
import br.com.servix.service.mapper.ServiceMapper;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.service.repository.ServiceSpecifications;
import java.math.BigDecimal;
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
public class ServiceService {

    private static final String[] ALLOWED_SORT_FIELDS = {
            "name", "durationMinutes", "price", "active", "createdAt", "updatedAt"
    };

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final TenantContextService tenantContextService;

    public ServiceResponse create(ServiceRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        ServiceRequest normalized = normalize(request);
        validateDuplicates(companyId, normalized.name(), null);
        ServiceOffering service = serviceMapper.toEntity(normalized);
        service.setCompanyId(companyId);
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceResponse> search(ServiceSearchRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        String sortBy = normalizeSortBy(request.sortBy());
        String direction = "DESC".equalsIgnoreCase(request.direction()) ? "DESC" : "ASC";
        Pageable pageable = PaginationUtils.toPageable(
                new PageRequestParams(
                        normalizePage(request.page()),
                        normalizeSize(request.size()),
                        sortBy,
                        direction,
                        request.filter()));

        Specification<ServiceOffering> specification = ServiceSpecifications.belongsToCompany(companyId)
                .and(ServiceSpecifications.genericFilter(request.filter()))
                .and(ServiceSpecifications.nameContains(request.name()))
                .and(ServiceSpecifications.activeEquals(request.active()))
                .and(ServiceSpecifications.priceBetween(request.minPrice(), request.maxPrice()))
                .and(ServiceSpecifications.durationBetween(request.minDuration(), request.maxDuration()));

        Page<ServiceResponse> page = serviceRepository.findAll(specification, pageable)
                .map(serviceMapper::toResponse);
        return PaginationUtils.fromPage(page, new PageRequestParams(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction,
                request.filter()));
    }

    @Transactional(readOnly = true)
    public ServiceResponse getById(UUID id) {
        return serviceMapper.toResponse(findService(id));
    }

    public ServiceResponse update(UUID id, ServiceRequest request) {
        ServiceOffering service = findService(id);
        ServiceRequest normalized = normalize(request);
        validateDuplicates(service.getCompanyId(), normalized.name(), service.getId());
        serviceMapper.apply(normalized, service);
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    public ServiceResponse updateStatus(UUID id, ServiceStatusUpdateRequest request) {
        ServiceOffering service = findService(id);
        service.setActive(Boolean.TRUE.equals(request.active()));
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    public void delete(UUID id) {
        serviceRepository.delete(findService(id));
    }

    private ServiceOffering findService(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return serviceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
    }

    private void validateDuplicates(UUID companyId, String name, UUID ignoreId) {
        if (ignoreId == null) {
            if (serviceRepository.existsByCompanyIdAndName(companyId, name)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço já cadastrado para a empresa");
            }
            return;
        }

        if (serviceRepository.existsByCompanyIdAndNameAndIdNot(companyId, name, ignoreId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço já cadastrado para a empresa");
        }
    }

    private ServiceRequest normalize(ServiceRequest request) {
        return new ServiceRequest(
                trim(request.name()),
                trim(request.description()),
                request.durationMinutes(),
                request.price() == null ? null : request.price().setScale(2, java.math.RoundingMode.HALF_UP));
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
}
