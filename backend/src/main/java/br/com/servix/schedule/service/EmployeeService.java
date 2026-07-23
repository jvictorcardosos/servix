package br.com.servix.schedule.service;

import br.com.servix.core.config.CoreConstants;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.core.pagination.PageRequestParams;
import br.com.servix.core.pagination.PaginationUtils;
import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.domain.WorkSchedule;
import br.com.servix.schedule.dto.EmployeeRequest;
import br.com.servix.schedule.dto.EmployeeResponse;
import br.com.servix.schedule.dto.EmployeeScheduleRequest;
import br.com.servix.schedule.dto.EmployeeSearchRequest;
import br.com.servix.schedule.dto.EmployeeStatusUpdateRequest;
import br.com.servix.schedule.mapper.EmployeeMapper;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.EmployeeSpecifications;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class EmployeeService {

    private static final String[] ALLOWED_SORT_FIELDS = {
            "name", "email", "phone", "active", "createdAt", "updatedAt"
    };

    private final EmployeeRepository employeeRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeMapper employeeMapper;
    private final TenantContextService tenantContextService;

    public EmployeeResponse create(EmployeeRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        EmployeeRequest normalized = normalize(request);
        validateDuplicateEmail(companyId, normalized.email(), null);

        Employee employee = employeeMapper.toEntity(normalized);
        employee.setCompanyId(companyId);
        Employee saved = employeeRepository.save(employee);
        replaceSchedules(saved.getId(), normalized.workSchedules());
        return employeeMapper.toResponse(saved, loadSchedules(saved.getId()));
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> search(EmployeeSearchRequest request) {
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

        Specification<Employee> specification = EmployeeSpecifications.belongsToCompany(companyId)
                .and(EmployeeSpecifications.genericFilter(request.filter()))
                .and(EmployeeSpecifications.nameContains(request.name()))
                .and(EmployeeSpecifications.emailContains(request.email()))
                .and(EmployeeSpecifications.phoneContains(request.phone()))
                .and(EmployeeSpecifications.activeEquals(request.active()));

        Page<EmployeeResponse> page = employeeRepository.findAll(specification, pageable)
                .map(employeeMapper::toSummaryResponse);

        return PaginationUtils.fromPage(page, new PageRequestParams(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction,
                request.filter()));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(UUID id) {
        Employee employee = findEmployee(id);
        return employeeMapper.toResponse(employee, loadSchedules(employee.getId()));
    }

    public EmployeeResponse update(UUID id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        EmployeeRequest normalized = normalize(request);
        validateDuplicateEmail(employee.getCompanyId(), normalized.email(), employee.getId());
        employeeMapper.apply(normalized, employee);
        Employee saved = employeeRepository.save(employee);
        replaceSchedules(saved.getId(), normalized.workSchedules());
        return employeeMapper.toResponse(saved, loadSchedules(saved.getId()));
    }

    public EmployeeResponse updateStatus(UUID id, EmployeeStatusUpdateRequest request) {
        Employee employee = findEmployee(id);
        employee.setActive(Boolean.TRUE.equals(request.active()));
        return employeeMapper.toResponse(employeeRepository.save(employee), loadSchedules(employee.getId()));
    }

    public void delete(UUID id) {
        Employee employee = findEmployee(id);
        workScheduleRepository.deleteByEmployeeId(employee.getId());
        employeeRepository.delete(employee);
    }

    private Employee findEmployee(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return employeeRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    private void validateDuplicateEmail(UUID companyId, String email, UUID ignoreId) {
        boolean exists = ignoreId == null
                ? employeeRepository.existsByCompanyIdAndEmail(companyId, email)
                : employeeRepository.existsByCompanyIdAndEmailAndIdNot(companyId, email, ignoreId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado para a empresa");
        }
    }

    private void replaceSchedules(UUID employeeId, List<EmployeeScheduleRequest> requests) {
        validateSchedules(requests);
        workScheduleRepository.deleteByEmployeeId(employeeId);

        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        List<WorkSchedule> schedules = new ArrayList<>();
        for (EmployeeScheduleRequest request : requests) {
            WorkSchedule schedule = employeeMapper.toScheduleEntity(normalizeSchedule(request), employee);
            schedules.add(schedule);
        }
        workScheduleRepository.saveAll(schedules);
    }

    private EmployeeScheduleRequest normalizeSchedule(EmployeeScheduleRequest request) {
        return new EmployeeScheduleRequest(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.active());
    }

    private void validateSchedules(List<EmployeeScheduleRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos uma jornada de trabalho");
        }

        Map<Integer, List<EmployeeScheduleRequest>> grouped = new LinkedHashMap<>();
        for (EmployeeScheduleRequest request : requests) {
            validateSingleSchedule(request);
            grouped.computeIfAbsent(request.dayOfWeek(), key -> new ArrayList<>()).add(request);
        }

        for (List<EmployeeScheduleRequest> schedules : grouped.values()) {
            schedules.sort(Comparator.comparing(EmployeeScheduleRequest::startTime));
            for (int i = 1; i < schedules.size(); i++) {
                EmployeeScheduleRequest previous = schedules.get(i - 1);
                EmployeeScheduleRequest current = schedules.get(i);
                if (isActive(previous) && isActive(current) && !current.startTime().isAfter(previous.endTime())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jornadas de trabalho sobrepostas");
                }
            }
        }
    }

    private void validateSingleSchedule(EmployeeScheduleRequest request) {
        if (request.dayOfWeek() == null || request.dayOfWeek() < 1 || request.dayOfWeek() > 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dia da semana inválido");
        }
        if (request.startTime() == null || request.endTime() == null || !request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário final deve ser maior que o inicial");
        }
    }

    private boolean isActive(EmployeeScheduleRequest request) {
        return request.active() == null || request.active();
    }

    private List<WorkSchedule> loadSchedules(UUID employeeId) {
        return workScheduleRepository.findByEmployeeIdOrderByDayOfWeekAscStartTimeAsc(employeeId);
    }

    private EmployeeRequest normalize(EmployeeRequest request) {
        List<EmployeeScheduleRequest> schedules = request.workSchedules().stream()
                .map(this::normalizeSchedule)
                .toList();
        return new EmployeeRequest(
                trim(request.name()),
                normalizeEmail(request.email()),
                trim(request.phone()),
                request.active(),
                schedules);
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

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
