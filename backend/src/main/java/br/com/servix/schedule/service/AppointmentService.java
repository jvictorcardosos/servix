package br.com.servix.schedule.service;

import br.com.servix.core.config.CoreConstants;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.core.pagination.PageRequestParams;
import br.com.servix.core.pagination.PaginationUtils;
import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.config.ScheduleProperties;
import br.com.servix.schedule.domain.Appointment;
import br.com.servix.schedule.domain.AppointmentStatus;
import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.domain.WorkSchedule;
import br.com.servix.schedule.dto.AppointmentRequest;
import br.com.servix.schedule.dto.AppointmentResponse;
import br.com.servix.schedule.dto.AppointmentSearchRequest;
import br.com.servix.schedule.dto.AppointmentStatusUpdateRequest;
import br.com.servix.schedule.mapper.AppointmentMapper;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.AppointmentSpecifications;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.repository.ServiceRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private static final String[] ALLOWED_SORT_FIELDS = {
            "appointmentDate", "startTime", "endTime", "status", "createdAt", "updatedAt"
    };

    private static final List<AppointmentStatus> CONFLICT_STATUSES = List.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.IN_PROGRESS);

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentMapper appointmentMapper;
    private final TenantContextService tenantContextService;
    private final ScheduleProperties scheduleProperties;

    public AppointmentResponse create(AppointmentRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        AppointmentContext context = resolveContext(companyId, request.customerId(), request.serviceId(), request.employeeId());
        LocalTime endTime = calculateEndTime(request.startTime(), context.service().getDurationMinutes());
        validateAppointmentDateTime(request.appointmentDate(), request.startTime());
        validateWorkingHours(context.employee().getId(), request.appointmentDate(), request.startTime(), endTime);
        validateConflicts(companyId, context.employee().getId(), context.customer().getId(), request.appointmentDate(), request.startTime(), endTime, null);

        Appointment appointment = new Appointment();
        appointment.setCompanyId(companyId);
        appointment.setCustomerId(context.customer().getId());
        appointment.setServiceId(context.service().getId());
        appointment.setEmployeeId(context.employee().getId());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(normalizeNotes(request.notes()));
        return mapToResponse(appointmentRepository.save(appointment), companyId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AppointmentResponse> search(AppointmentSearchRequest request) {
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

        Specification<Appointment> specification = buildSpecification(companyId, request.filter(), request.customerId(), request.serviceId(), request.employeeId(), request.status(), request.dateFrom(), request.dateTo());
        Page<Appointment> page = appointmentRepository.findAll(specification, pageable);
        Map<UUID, Customer> customers = loadCustomers(companyId, extractIds(page.getContent(), Appointment::getCustomerId));
        Map<UUID, ServiceOffering> services = loadServices(companyId, extractIds(page.getContent(), Appointment::getServiceId));
        Map<UUID, Employee> employees = loadEmployees(companyId, extractIds(page.getContent(), Appointment::getEmployeeId));
        Page<AppointmentResponse> responsePage = page.map(appointment -> toResponse(appointment, customers, services, employees));

        return PaginationUtils.fromPage(responsePage, new PageRequestParams(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction,
                request.filter()));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return mapToResponse(findAppointment(id), companyId);
    }

    public AppointmentResponse update(UUID id, AppointmentRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        Appointment appointment = findAppointment(id);
        AppointmentContext context = resolveContext(companyId, request.customerId(), request.serviceId(), request.employeeId());
        LocalTime endTime = calculateEndTime(request.startTime(), context.service().getDurationMinutes());
        validateAppointmentDateTime(request.appointmentDate(), request.startTime());
        validateWorkingHours(context.employee().getId(), request.appointmentDate(), request.startTime(), endTime);
        validateConflicts(companyId, context.employee().getId(), context.customer().getId(), request.appointmentDate(), request.startTime(), endTime, appointment.getId());

        appointment.setCustomerId(context.customer().getId());
        appointment.setServiceId(context.service().getId());
        appointment.setEmployeeId(context.employee().getId());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(endTime);
        appointment.setNotes(normalizeNotes(request.notes()));
        return mapToResponse(appointmentRepository.save(appointment), companyId);
    }

    public AppointmentResponse updateStatus(UUID id, AppointmentStatusUpdateRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        Appointment appointment = findAppointment(id);
        appointment.setStatus(request.status());
        return mapToResponse(appointmentRepository.save(appointment), companyId);
    }

    public void delete(UUID id) {
        appointmentRepository.delete(findAppointment(id));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listDay(LocalDate date, UUID employeeId, UUID customerId, UUID serviceId, AppointmentStatus status) {
        LocalDate reference = date == null ? LocalDate.now() : date;
        return listByRange(reference, reference, employeeId, customerId, serviceId, status);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listWeek(LocalDate date, UUID employeeId, UUID customerId, UUID serviceId, AppointmentStatus status) {
        LocalDate reference = date == null ? LocalDate.now() : date;
        LocalDate from = reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = from.plusDays(6);
        return listByRange(from, to, employeeId, customerId, serviceId, status);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listMonth(LocalDate date, UUID employeeId, UUID customerId, UUID serviceId, AppointmentStatus status) {
        LocalDate reference = date == null ? LocalDate.now() : date;
        LocalDate from = reference.withDayOfMonth(1);
        LocalDate to = reference.with(TemporalAdjusters.lastDayOfMonth());
        return listByRange(from, to, employeeId, customerId, serviceId, status);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listByEmployee(UUID employeeId, LocalDate from, LocalDate to) {
        return listByRange(resolveFrom(from), resolveTo(to), employeeId, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listByCustomer(UUID customerId, LocalDate from, LocalDate to) {
        return listByRange(resolveFrom(from), resolveTo(to), null, customerId, null, null);
    }

    private Appointment findAppointment(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return appointmentRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
    }

    private Specification<Appointment> buildSpecification(
            UUID companyId,
            String filter,
            UUID customerId,
            UUID serviceId,
            UUID employeeId,
            AppointmentStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {
        return AppointmentSpecifications.belongsToCompany(companyId)
                .and(AppointmentSpecifications.genericFilter(filter))
                .and(AppointmentSpecifications.customerEquals(customerId))
                .and(AppointmentSpecifications.serviceEquals(serviceId))
                .and(AppointmentSpecifications.employeeEquals(employeeId))
                .and(AppointmentSpecifications.statusEquals(status))
                .and(AppointmentSpecifications.dateFrom(dateFrom))
                .and(AppointmentSpecifications.dateTo(dateTo));
    }

    private AppointmentContext resolveContext(UUID companyId, UUID customerId, UUID serviceId, UUID employeeId) {
        Customer customer = customerRepository.findByIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        if (!customer.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inativo");
        }

        ServiceOffering service = serviceRepository.findByIdAndCompanyId(serviceId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
        if (!service.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço inativo");
        }

        Employee employee = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
        if (!employee.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Funcionário inativo");
        }

        return new AppointmentContext(customer, service, employee);
    }

    private void validateAppointmentDateTime(LocalDate date, LocalTime startTime) {
        if (scheduleProperties.isAllowPastAppointments()) {
            return;
        }
        int tolerance = Math.max(0, scheduleProperties.getPastAppointmentToleranceMinutes());
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime minimumAllowed = LocalDateTime.now().minusMinutes(tolerance);
        if (appointmentDateTime.isBefore(minimumAllowed)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agendamento no passado não é permitido");
        }
    }

    private LocalTime calculateEndTime(LocalTime startTime, Integer durationMinutes) {
        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário final deve ser maior que o inicial");
        }
        return endTime;
    }

    private void validateWorkingHours(UUID employeeId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<WorkSchedule> schedules = workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrueOrderByStartTimeAsc(
                employeeId,
                date.getDayOfWeek().getValue());
        boolean matches = schedules.stream().anyMatch(schedule ->
                !startTime.isBefore(schedule.getStartTime()) && !endTime.isAfter(schedule.getEndTime()));
        if (!matches) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário fora da jornada do funcionário");
        }
    }

    private void validateConflicts(
            UUID companyId,
            UUID employeeId,
            UUID customerId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            UUID ignoreId) {
        if (appointmentRepository.existsEmployeeConflict(companyId, employeeId, date, startTime, endTime, CONFLICT_STATUSES, ignoreId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Funcionário já possui agendamento nesse horário");
        }
        if (appointmentRepository.existsCustomerConflict(companyId, customerId, date, startTime, endTime, CONFLICT_STATUSES, ignoreId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cliente já possui agendamento nesse horário");
        }
    }

    private AppointmentResponse mapToResponse(Appointment appointment, UUID companyId) {
        Map<UUID, Customer> customers = loadCustomers(companyId, Set.of(appointment.getCustomerId()));
        Map<UUID, ServiceOffering> services = loadServices(companyId, Set.of(appointment.getServiceId()));
        Map<UUID, Employee> employees = loadEmployees(companyId, Set.of(appointment.getEmployeeId()));
        return toResponse(appointment, customers, services, employees);
    }

    private AppointmentResponse toResponse(
            Appointment appointment,
            Map<UUID, Customer> customers,
            Map<UUID, ServiceOffering> services,
            Map<UUID, Employee> employees) {
        return appointmentMapper.toResponse(
                appointment,
                customers.getOrDefault(appointment.getCustomerId(), null) == null ? null : customers.get(appointment.getCustomerId()).getNome(),
                services.getOrDefault(appointment.getServiceId(), null) == null ? null : services.get(appointment.getServiceId()).getName(),
                employees.getOrDefault(appointment.getEmployeeId(), null) == null ? null : employees.get(appointment.getEmployeeId()).getName());
    }

    private List<AppointmentResponse> listByRange(
            LocalDate from,
            LocalDate to,
            UUID employeeId,
            UUID customerId,
            UUID serviceId,
            AppointmentStatus status) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        Specification<Appointment> specification = buildSpecification(companyId, null, customerId, serviceId, employeeId, status, from, to);
        List<Appointment> appointments = appointmentRepository.findAll(
                specification,
                Sort.by(Sort.Direction.ASC, "appointmentDate").and(Sort.by(Sort.Direction.ASC, "startTime")));
        Map<UUID, String> customerNames = loadCustomers(companyId, extractIds(appointments, Appointment::getCustomerId))
                .values().stream().collect(java.util.stream.Collectors.toMap(Customer::getId, Customer::getNome));
        Map<UUID, String> serviceNames = loadServices(companyId, extractIds(appointments, Appointment::getServiceId))
                .values().stream().collect(java.util.stream.Collectors.toMap(ServiceOffering::getId, ServiceOffering::getName));
        Map<UUID, String> employeeNames = loadEmployees(companyId, extractIds(appointments, Appointment::getEmployeeId))
                .values().stream().collect(java.util.stream.Collectors.toMap(Employee::getId, Employee::getName));
        List<AppointmentResponse> responses = new ArrayList<>();
        for (Appointment appointment : appointments) {
            responses.add(appointmentMapper.toResponse(
                    appointment,
                    customerNames.get(appointment.getCustomerId()),
                    serviceNames.get(appointment.getServiceId()),
                    employeeNames.get(appointment.getEmployeeId())));
        }
        return responses;
    }

    private LocalDate resolveFrom(LocalDate from) {
        return from == null ? LocalDate.now().withDayOfMonth(1) : from;
    }

    private LocalDate resolveTo(LocalDate to) {
        return to == null ? LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()) : to;
    }

    private Map<UUID, Customer> loadCustomers(UUID companyId, Collection<UUID> ids) {
        Map<UUID, Customer> customers = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return customers;
        }
        for (Customer customer : customerRepository.findAllByCompanyIdAndIdIn(companyId, ids)) {
            customers.put(customer.getId(), customer);
        }
        return customers;
    }

    private Map<UUID, ServiceOffering> loadServices(UUID companyId, Collection<UUID> ids) {
        Map<UUID, ServiceOffering> services = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return services;
        }
        for (ServiceOffering service : serviceRepository.findAllByCompanyIdAndIdIn(companyId, ids)) {
            services.put(service.getId(), service);
        }
        return services;
    }

    private Map<UUID, Employee> loadEmployees(UUID companyId, Collection<UUID> ids) {
        Map<UUID, Employee> employees = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return employees;
        }
        for (Employee employee : employeeRepository.findAllByCompanyIdAndIdIn(companyId, ids)) {
            employees.put(employee.getId(), employee);
        }
        return employees;
    }

    private <T> Set<UUID> extractIds(List<Appointment> appointments, java.util.function.Function<Appointment, UUID> extractor) {
        return appointments.stream().map(extractor).collect(java.util.stream.Collectors.toSet());
    }

    private String normalizeNotes(String notes) {
        return notes == null ? null : notes.trim();
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
            return "appointmentDate";
        }
        for (String allowed : ALLOWED_SORT_FIELDS) {
            if (allowed.equalsIgnoreCase(sortBy)) {
                return allowed;
            }
        }
        return "appointmentDate";
    }

    private record AppointmentContext(Customer customer, ServiceOffering service, Employee employee) {
    }
}
