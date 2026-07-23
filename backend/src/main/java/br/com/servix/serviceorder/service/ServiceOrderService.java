package br.com.servix.serviceorder.service;

import br.com.servix.core.config.CoreConstants;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.core.pagination.PageRequestParams;
import br.com.servix.core.pagination.PaginationUtils;
import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.domain.Appointment;
import br.com.servix.schedule.domain.AppointmentStatus;
import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.serviceorder.domain.ServiceOrder;
import br.com.servix.serviceorder.domain.ServiceOrderHistory;
import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import br.com.servix.serviceorder.dto.ServiceOrderHistoryResponse;
import br.com.servix.serviceorder.dto.ServiceOrderRequest;
import br.com.servix.serviceorder.dto.ServiceOrderResponse;
import br.com.servix.serviceorder.dto.ServiceOrderSearchRequest;
import br.com.servix.serviceorder.mapper.ServiceOrderMapper;
import br.com.servix.serviceorder.repository.ServiceOrderHistoryRepository;
import br.com.servix.serviceorder.repository.ServiceOrderRepository;
import br.com.servix.serviceorder.repository.ServiceOrderSpecifications;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
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
public class ServiceOrderService {

    private static final String[] ALLOWED_SORT_FIELDS = {
            "scheduledStart", "scheduledEnd", "status", "createdAt", "updatedAt", "estimatedDuration", "actualDuration"
    };

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderHistoryRepository serviceOrderHistoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceOrderMapper serviceOrderMapper;
    private final TenantContextService tenantContextService;

    public ServiceOrderResponse create(ServiceOrderRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        ServiceOrder order;
        if (request.appointmentId() != null) {
            order = createFromAppointment(companyId, request);
        } else {
            order = createManual(companyId, request);
        }
        ServiceOrder saved = serviceOrderRepository.save(order);
        appendHistory(saved, null, saved.getStatus(), historyObservation(saved.getObservations(), saved.getStatus()));
        return mapToResponse(saved, companyId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceOrderResponse> search(ServiceOrderSearchRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return searchInternal(companyId, request, request.customerId(), request.professionalId(), request.serviceId(), request.appointmentId(), request.status());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceOrderResponse> searchByCustomer(UUID customerId, ServiceOrderSearchRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return searchInternal(companyId, request, customerId, request.professionalId(), request.serviceId(), request.appointmentId(), request.status());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceOrderResponse> searchByProfessional(UUID professionalId, ServiceOrderSearchRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return searchInternal(companyId, request, request.customerId(), professionalId, request.serviceId(), request.appointmentId(), request.status());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceOrderResponse> searchByStatus(ServiceOrderStatus status, ServiceOrderSearchRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return searchInternal(companyId, request, request.customerId(), request.professionalId(), request.serviceId(), request.appointmentId(), status);
    }

    @Transactional(readOnly = true)
    public ServiceOrderResponse getById(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return mapToResponse(findOrder(id), companyId);
    }

    public ServiceOrderResponse update(UUID id, ServiceOrderRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        ServiceOrder order = findOrder(id);
        ensureMutable(order);

        ServiceOrderResolvedData resolved = resolveData(companyId, order, request, false);
        applyResolvedData(order, resolved);
        order.setObservations(normalizeObservations(request.observations()));
        syncAppointment(order, resolved);
        return mapToResponse(serviceOrderRepository.save(order), companyId);
    }

    public void delete(UUID id) {
        ServiceOrder order = findOrder(id);
        if (order.getStatus() == ServiceOrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordem de serviço concluída não pode ser excluída");
        }
        serviceOrderRepository.delete(order);
    }

    public ServiceOrderResponse start(UUID id) {
        return changeStatus(id, ServiceOrderStatus.IN_PROGRESS, "Atendimento iniciado");
    }

    public ServiceOrderResponse pause(UUID id) {
        return changeStatus(id, ServiceOrderStatus.PAUSED, "Atendimento pausado");
    }

    public ServiceOrderResponse resume(UUID id) {
        return changeStatus(id, ServiceOrderStatus.IN_PROGRESS, "Atendimento retomado");
    }

    public ServiceOrderResponse finish(UUID id) {
        ServiceOrder order = findOrder(id);
        if (order.getStatus() == ServiceOrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordem cancelada não pode ser concluída");
        }
        if (order.getStartedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível concluir sem iniciar");
        }
        if (order.getStatus() != ServiceOrderStatus.IN_PROGRESS && order.getStatus() != ServiceOrderStatus.PAUSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordem em estado inválido para conclusão");
        }
        return complete(order, "Atendimento concluído");
    }

    public ServiceOrderResponse cancel(UUID id) {
        ServiceOrder order = findOrder(id);
        if (order.getStatus() == ServiceOrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordem concluída não pode ser cancelada");
        }
        return changeStatus(order, ServiceOrderStatus.CANCELLED, "Ordem cancelada");
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderHistoryResponse> history(UUID id) {
        ServiceOrder order = findOrder(id);
        return serviceOrderHistoryRepository.findAllByServiceOrderIdOrderByChangedAtAsc(order.getId())
                .stream()
                .map(serviceOrderMapper::toHistoryResponse)
                .toList();
    }

    private PagedResponse<ServiceOrderResponse> searchInternal(
            UUID companyId,
            ServiceOrderSearchRequest request,
            UUID customerId,
            UUID professionalId,
            UUID serviceId,
            UUID appointmentId,
            ServiceOrderStatus status) {
        String sortBy = normalizeSortBy(request.sortBy());
        String direction = "DESC".equalsIgnoreCase(request.direction()) ? "DESC" : "ASC";
        Pageable pageable = PaginationUtils.toPageable(
                new PageRequestParams(
                        normalizePage(request.page()),
                        normalizeSize(request.size()),
                        sortBy,
                        direction,
                        request.filter()));

        Specification<ServiceOrder> specification = ServiceOrderSpecifications.belongsToCompany(companyId)
                .and(ServiceOrderSpecifications.genericFilter(request.filter()))
                .and(ServiceOrderSpecifications.customerEquals(customerId))
                .and(ServiceOrderSpecifications.professionalEquals(professionalId))
                .and(ServiceOrderSpecifications.serviceEquals(serviceId))
                .and(ServiceOrderSpecifications.appointmentEquals(appointmentId))
                .and(ServiceOrderSpecifications.statusEquals(status))
                .and(ServiceOrderSpecifications.scheduledStartFrom(request.dateFrom()))
                .and(ServiceOrderSpecifications.scheduledStartTo(request.dateTo()));

        Page<ServiceOrder> page = serviceOrderRepository.findAll(specification, pageable);
        Map<UUID, Customer> customers = loadCustomers(companyId, extractIds(page.getContent(), ServiceOrder::getCustomerId));
        Map<UUID, Employee> professionals = loadEmployees(companyId, extractIds(page.getContent(), ServiceOrder::getProfessionalId));
        Map<UUID, ServiceOffering> services = loadServices(companyId, extractIds(page.getContent(), ServiceOrder::getServiceId));
        Page<ServiceOrderResponse> responsePage = page.map(order -> toResponse(order, customers, professionals, services));

        return PaginationUtils.fromPage(responsePage, new PageRequestParams(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction,
                request.filter()));
    }

    private ServiceOrder createManual(UUID companyId, ServiceOrderRequest request) {
        ServiceOrderResolvedData resolved = resolveData(companyId, null, request, true);
        ServiceOrder order = new ServiceOrder();
        order.setCompanyId(companyId);
        applyResolvedData(order, resolved);
        order.setObservations(normalizeObservations(request.observations()));
        order.setAppointmentId(null);
        order.setStatus(ServiceOrderStatus.OPEN);
        order.setStartedAt(null);
        order.setFinishedAt(null);
        order.setActualDuration(null);
        return order;
    }

    private ServiceOrder createFromAppointment(UUID companyId, ServiceOrderRequest request) {
        Appointment appointment = findAppointment(companyId, request.appointmentId());
        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agendamento não pode gerar ordem de serviço");
        }
        if (serviceOrderRepository.existsByCompanyIdAndAppointmentId(companyId, appointment.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe ordem de serviço para este agendamento");
        }

        ServiceOrderResolvedData resolved = resolveDataFromAppointment(companyId, appointment, request);
        ServiceOrder order = new ServiceOrder();
        order.setCompanyId(companyId);
        applyResolvedData(order, resolved);
        order.setObservations(normalizeObservations(request.observations()));
        order.setAppointmentId(appointment.getId());
        order.setStatus(ServiceOrderStatus.CONFIRMED);
        order.setStartedAt(null);
        order.setFinishedAt(null);
        order.setActualDuration(null);
        syncAppointmentStatus(appointment, ServiceOrderStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        return order;
    }

    private ServiceOrderResponse changeStatus(UUID id, ServiceOrderStatus newStatus, String observation) {
        ServiceOrder order = findOrder(id);
        return changeStatus(order, newStatus, observation);
    }

    private ServiceOrderResponse changeStatus(ServiceOrder order, ServiceOrderStatus newStatus, String observation) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        ServiceOrderStatus previousStatus = order.getStatus();

        if (newStatus == ServiceOrderStatus.IN_PROGRESS) {
            if (order.getStatus() == ServiceOrderStatus.PAUSED) {
                ensureNoActiveOrderForProfessional(companyId, order.getProfessionalId(), order.getId());
            } else if (order.getStatus() != ServiceOrderStatus.OPEN && order.getStatus() != ServiceOrderStatus.CONFIRMED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordem em estado inválido para início");
            } else {
                ensureNoActiveOrderForProfessional(companyId, order.getProfessionalId(), order.getId());
            }
            if (order.getStartedAt() == null) {
                order.setStartedAt(LocalDateTime.now());
            }
        } else if (newStatus == ServiceOrderStatus.PAUSED) {
            if (order.getStatus() != ServiceOrderStatus.IN_PROGRESS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordem em estado inválido para pausa");
            }
        } else if (newStatus == ServiceOrderStatus.CANCELLED) {
            if (order.getStatus() == ServiceOrderStatus.COMPLETED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordem concluída não pode ser cancelada");
            }
        }

        order.setStatus(newStatus);
        if (order.getAppointmentId() != null) {
            Appointment appointment = findAppointment(companyId, order.getAppointmentId());
            syncAppointmentStatus(appointment, newStatus);
            appointmentRepository.save(appointment);
        }

        ServiceOrder saved = serviceOrderRepository.save(order);
        appendHistory(saved, previousStatus, newStatus, observation);
        return mapToResponse(saved, companyId);
    }

    private ServiceOrderResponse complete(ServiceOrder order, String observation) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        ServiceOrderStatus previousStatus = order.getStatus();
        order.setStatus(ServiceOrderStatus.COMPLETED);
        order.setFinishedAt(LocalDateTime.now());
        order.setActualDuration((int) ChronoUnit.MINUTES.between(order.getStartedAt(), order.getFinishedAt()));
        if (order.getAppointmentId() != null) {
            Appointment appointment = findAppointment(companyId, order.getAppointmentId());
            syncAppointmentStatus(appointment, ServiceOrderStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }
        ServiceOrder saved = serviceOrderRepository.save(order);
        appendHistory(saved, previousStatus, ServiceOrderStatus.COMPLETED, observation);
        return mapToResponse(saved, companyId);
    }

    private void syncAppointment(ServiceOrder order, ServiceOrderResolvedData resolved) {
        if (order.getAppointmentId() == null) {
            return;
        }
        UUID companyId = tenantContextService.getRequiredTenantId();
        Appointment appointment = findAppointment(companyId, order.getAppointmentId());
        appointment.setCustomerId(order.getCustomerId());
        appointment.setServiceId(order.getServiceId());
        appointment.setEmployeeId(order.getProfessionalId());
        appointment.setAppointmentDate(order.getScheduledStart().toLocalDate());
        appointment.setStartTime(order.getScheduledStart().toLocalTime());
        appointment.setEndTime(order.getScheduledEnd().toLocalTime());
        syncAppointmentStatus(appointment, order.getStatus());
        appointmentRepository.save(appointment);
    }

    private void syncAppointmentStatus(Appointment appointment, ServiceOrderStatus status) {
        appointment.setStatus(mapAppointmentStatus(status));
    }

    private AppointmentStatus mapAppointmentStatus(ServiceOrderStatus status) {
        return switch (status) {
            case OPEN -> AppointmentStatus.SCHEDULED;
            case CONFIRMED -> AppointmentStatus.CONFIRMED;
            case IN_PROGRESS, PAUSED -> AppointmentStatus.IN_PROGRESS;
            case COMPLETED -> AppointmentStatus.COMPLETED;
            case CANCELLED -> AppointmentStatus.CANCELLED;
            case NO_SHOW -> AppointmentStatus.NO_SHOW;
        };
    }

    private void ensureMutable(ServiceOrder order) {
        if (order.getStatus() == ServiceOrderStatus.COMPLETED || order.getStatus() == ServiceOrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordem finalizada não pode ser alterada");
        }
    }

    private void ensureNoActiveOrderForProfessional(UUID companyId, UUID professionalId, UUID ignoreId) {
        if (serviceOrderRepository.existsByCompanyIdAndProfessionalIdAndStatusAndIdNot(
                companyId,
                professionalId,
                ServiceOrderStatus.IN_PROGRESS,
                ignoreId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profissional já possui ordem de serviço em andamento");
        }
    }

    private ServiceOrderResolvedData resolveData(UUID companyId, ServiceOrder currentOrder, ServiceOrderRequest request, boolean creating) {
        if (currentOrder != null && currentOrder.getAppointmentId() != null) {
            Appointment appointment = findAppointment(companyId, currentOrder.getAppointmentId());
            return resolveDataFromAppointment(companyId, appointment, request);
        }

        UUID customerId = request.customerId() != null ? request.customerId() : currentOrder == null ? null : currentOrder.getCustomerId();
        UUID professionalId = request.professionalId() != null ? request.professionalId() : currentOrder == null ? null : currentOrder.getProfessionalId();
        UUID serviceId = request.serviceId() != null ? request.serviceId() : currentOrder == null ? null : currentOrder.getServiceId();
        LocalDateTime scheduledStart = request.scheduledStart() != null ? request.scheduledStart() : currentOrder == null ? null : currentOrder.getScheduledStart();

        if (creating) {
            if (customerId == null || professionalId == null || serviceId == null || scheduledStart == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos obrigatórios para ordem manual não informados");
            }
        }

        Customer customer = customerId == null ? null : findCustomer(companyId, customerId);
        Employee professional = professionalId == null ? null : findProfessional(companyId, professionalId);
        ServiceOffering service = serviceId == null ? null : findService(companyId, serviceId);

        if (customer != null && !customer.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inativo");
        }
        if (professional != null && !professional.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profissional inativo");
        }
        if (service != null && !service.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço inativo");
        }

        BigDecimal servicePrice = service == null ? currentOrder.getServicePrice() : service.getPrice();
        Integer estimatedDuration = service == null ? currentOrder.getEstimatedDuration() : service.getDurationMinutes();
        LocalDateTime resolvedScheduledStart = scheduledStart;
        LocalDateTime scheduledEnd = resolvedScheduledStart == null || estimatedDuration == null ? null : resolvedScheduledStart.plusMinutes(estimatedDuration);
        if (resolvedScheduledStart != null && scheduledEnd != null && !scheduledEnd.isAfter(resolvedScheduledStart)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário final deve ser maior que o inicial");
        }

        return new ServiceOrderResolvedData(customer, professional, service, servicePrice, estimatedDuration, resolvedScheduledStart, scheduledEnd);
    }

    private ServiceOrderResolvedData resolveDataFromAppointment(UUID companyId, Appointment appointment, ServiceOrderRequest request) {
        Customer customer = findCustomer(companyId, appointment.getCustomerId());
        Employee professional = findProfessional(companyId, appointment.getEmployeeId());
        ServiceOffering service = findService(companyId, appointment.getServiceId());

        if (!customer.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inativo");
        }
        if (!professional.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profissional inativo");
        }
        if (!service.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço inativo");
        }

        LocalDateTime scheduledStart = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime());
        LocalDateTime scheduledEnd = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getEndTime());

        validateAppointmentConsistency(request, appointment, customer.getId(), professional.getId(), service.getId(), scheduledStart);

        return new ServiceOrderResolvedData(customer, professional, service, service.getPrice(), service.getDurationMinutes(), scheduledStart, scheduledEnd);
    }

    private void validateAppointmentConsistency(
            ServiceOrderRequest request,
            Appointment appointment,
            UUID customerId,
            UUID professionalId,
            UUID serviceId,
            LocalDateTime scheduledStart) {
        if (request.customerId() != null && !request.customerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente não corresponde ao agendamento");
        }
        if (request.professionalId() != null && !request.professionalId().equals(professionalId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profissional não corresponde ao agendamento");
        }
        if (request.serviceId() != null && !request.serviceId().equals(serviceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço não corresponde ao agendamento");
        }
        if (request.scheduledStart() != null && !request.scheduledStart().equals(scheduledStart)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário não corresponde ao agendamento");
        }
    }

    private void applyResolvedData(ServiceOrder order, ServiceOrderResolvedData resolved) {
        order.setCustomerId(resolved.customer().getId());
        order.setProfessionalId(resolved.professional().getId());
        order.setServiceId(resolved.service().getId());
        order.setServicePrice(resolved.servicePrice());
        order.setEstimatedDuration(resolved.estimatedDuration());
        order.setScheduledStart(resolved.scheduledStart());
        order.setScheduledEnd(resolved.scheduledEnd());
    }

    private ServiceOrderResponse mapToResponse(ServiceOrder order, UUID companyId) {
        Map<UUID, Customer> customers = loadCustomers(companyId, Set.of(order.getCustomerId()));
        Map<UUID, Employee> professionals = loadEmployees(companyId, Set.of(order.getProfessionalId()));
        Map<UUID, ServiceOffering> services = loadServices(companyId, Set.of(order.getServiceId()));
        return toResponse(order, customers, professionals, services);
    }

    private ServiceOrderResponse toResponse(
            ServiceOrder order,
            Map<UUID, Customer> customers,
            Map<UUID, Employee> professionals,
            Map<UUID, ServiceOffering> services) {
        return serviceOrderMapper.toResponse(
                order,
                customers.get(order.getCustomerId()) == null ? null : customers.get(order.getCustomerId()).getNome(),
                professionals.get(order.getProfessionalId()) == null ? null : professionals.get(order.getProfessionalId()).getName(),
                services.get(order.getServiceId()) == null ? null : services.get(order.getServiceId()).getName());
    }

    private ServiceOrder findOrder(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return serviceOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada"));
    }

    private Appointment findAppointment(UUID companyId, UUID appointmentId) {
        return appointmentRepository.findByIdAndCompanyId(appointmentId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
    }

    private Customer findCustomer(UUID companyId, UUID customerId) {
        return customerRepository.findByIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    private Employee findProfessional(UUID companyId, UUID professionalId) {
        return employeeRepository.findByIdAndCompanyId(professionalId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado"));
    }

    private ServiceOffering findService(UUID companyId, UUID serviceId) {
        return serviceRepository.findByIdAndCompanyId(serviceId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
    }

    private void appendHistory(ServiceOrder order, ServiceOrderStatus previousStatus, ServiceOrderStatus newStatus, String observation) {
        ServiceOrderHistory history = new ServiceOrderHistory();
        history.setServiceOrderId(order.getId());
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(tenantContextService.getRequiredUserId());
        history.setChangedAt(LocalDateTime.now(ZoneId.systemDefault()));
        history.setObservation(observation);
        serviceOrderHistoryRepository.save(history);
    }

    private String historyObservation(String observations, ServiceOrderStatus status) {
        if (observations != null && !observations.isBlank()) {
            return observations.trim();
        }
        return switch (status) {
            case OPEN -> "Ordem criada manualmente";
            case CONFIRMED -> "Ordem criada a partir do agendamento";
            case IN_PROGRESS -> "Atendimento iniciado";
            case PAUSED -> "Atendimento pausado";
            case COMPLETED -> "Atendimento concluído";
            case CANCELLED -> "Ordem cancelada";
            case NO_SHOW -> "Cliente não compareceu";
        };
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

    private String normalizeObservations(String observations) {
        return observations == null || observations.isBlank() ? null : observations.trim();
    }

    private <T, ID> Collection<ID> extractIds(List<T> content, java.util.function.Function<T, ID> extractor) {
        Map<ID, Boolean> ids = new LinkedHashMap<>();
        for (T item : content) {
            ID id = extractor.apply(item);
            if (id != null) {
                ids.put(id, Boolean.TRUE);
            }
        }
        return ids.keySet();
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

    private record ServiceOrderResolvedData(
            Customer customer,
            Employee professional,
            ServiceOffering service,
            BigDecimal servicePrice,
            Integer estimatedDuration,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd) {
    }
}
