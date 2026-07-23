package br.com.servix.billing.service;

import br.com.servix.billing.config.FinancialProperties;
import br.com.servix.billing.domain.FinancialStatus;
import br.com.servix.billing.domain.FinancialTransaction;
import br.com.servix.billing.domain.PaymentMethod;
import br.com.servix.billing.domain.TransactionType;
import br.com.servix.billing.dto.FinancialAdjustmentRequest;
import br.com.servix.billing.dto.FinancialPaymentRequest;
import br.com.servix.billing.dto.FinancialTransactionRequest;
import br.com.servix.billing.dto.FinancialTransactionResponse;
import br.com.servix.billing.dto.FinancialTransactionSearchRequest;
import br.com.servix.billing.mapper.FinancialMapper;
import br.com.servix.billing.repository.FinancialSpecifications;
import br.com.servix.billing.repository.FinancialTransactionRepository;
import br.com.servix.core.config.CoreConstants;
import br.com.servix.core.pagination.PagedResponse;
import br.com.servix.core.pagination.PageRequestParams;
import br.com.servix.core.pagination.PaginationUtils;
import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.serviceorder.domain.ServiceOrder;
import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import br.com.servix.serviceorder.repository.ServiceOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class FinancialService {

    private static final String[] ALLOWED_SORT_FIELDS = {
            "dueDate", "paymentDate", "status", "amount", "totalAmount", "paidAmount", "createdAt", "updatedAt"
    };

    private final FinancialTransactionRepository financialTransactionRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;
    private final PaymentMethodService paymentMethodService;
    private final FinancialMapper financialMapper;
    private final TenantContextService tenantContextService;
    private final FinancialProperties financialProperties;

    public FinancialTransactionResponse create(FinancialTransactionRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        FinancialTransaction transaction = request.serviceOrderId() == null
                ? createManual(companyId, request)
                : createFromServiceOrder(companyId, request.serviceOrderId(), request);
        FinancialTransaction saved = financialTransactionRepository.save(transaction);
        return mapToResponse(saved, companyId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FinancialTransactionResponse> search(FinancialTransactionSearchRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        String sortBy = normalizeSortBy(request.sortBy());
        String direction = "DESC".equalsIgnoreCase(request.direction()) ? "DESC" : "ASC";
        Pageable pageable = PaginationUtils.toPageable(new PageRequestParams(
                normalizePage(request.page()),
                normalizeSize(request.size()),
                sortBy,
                direction,
                request.filter()));

        Specification<FinancialTransaction> specification = buildSpecification(companyId, request);
        Page<FinancialTransaction> page = financialTransactionRepository.findAll(specification, pageable);
        Map<UUID, Customer> customers = loadCustomers(companyId, extractIds(page.getContent(), FinancialTransaction::getCustomerId));
        Map<UUID, Employee> professionals = loadProfessionals(companyId, extractIds(page.getContent(), FinancialTransaction::getProfessionalId));
        Map<UUID, ServiceOffering> services = loadServices(companyId, extractIds(page.getContent(), FinancialTransaction::getServiceId));
        Map<UUID, PaymentMethod> paymentMethods = loadPaymentMethods(companyId, extractIds(page.getContent(), FinancialTransaction::getPaymentMethodId));
        Page<FinancialTransactionResponse> responsePage = page.map(transaction -> financialMapper.toResponse(
                transaction,
                customers.get(transaction.getCustomerId()) == null ? null : customers.get(transaction.getCustomerId()).getNome(),
                professionals.get(transaction.getProfessionalId()) == null ? null : professionals.get(transaction.getProfessionalId()).getName(),
                services.get(transaction.getServiceId()) == null ? null : services.get(transaction.getServiceId()).getName(),
                paymentMethods.get(transaction.getPaymentMethodId()) == null ? null : paymentMethods.get(transaction.getPaymentMethodId()).getName()));

        return PaginationUtils.fromPage(responsePage, new PageRequestParams(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                direction,
                request.filter()));
    }

    @Transactional(readOnly = true)
    public FinancialTransactionResponse getById(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return mapToResponse(findTransaction(id), companyId);
    }

    public FinancialTransactionResponse update(UUID id, FinancialTransactionRequest request) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        FinancialTransaction transaction = findTransaction(id);
        ensureMutable(transaction);
        applyRequest(transaction, request, companyId);
        recalculate(transaction);
        return mapToResponse(financialTransactionRepository.save(transaction), companyId);
    }

    public void delete(UUID id) {
        FinancialTransaction transaction = findTransaction(id);
        if (transaction.getPaidAmount().compareTo(BigDecimal.ZERO) > 0 || transaction.getStatus() == FinancialStatus.PAID || transaction.getStatus() == FinancialStatus.PARTIALLY_PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lançamentos pagos não podem ser excluídos");
        }
        financialTransactionRepository.delete(transaction);
    }

    public FinancialTransactionResponse pay(UUID id, FinancialPaymentRequest request) {
        FinancialTransaction transaction = findTransaction(id);
        ensureMutableForPayment(transaction);
        BigDecimal amount = scale(request.amount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor do pagamento deve ser maior que zero");
        }
        BigDecimal remaining = transaction.getTotalAmount().subtract(transaction.getPaidAmount());
        if (amount.compareTo(remaining) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor do pagamento excede o saldo pendente");
        }
        transaction.setPaidAmount(transaction.getPaidAmount().add(amount));
        transaction.setPaymentDate(request.paymentDate() == null ? LocalDate.now() : request.paymentDate());
        transaction.setPaymentMethodId(request.paymentMethodId());
        mergeDescription(transaction, request.description());
        transaction.setExternalReference(normalizeText(request.externalReference(), transaction.getExternalReference()));
        updateStatusAfterPayment(transaction);
        return mapToResponse(financialTransactionRepository.save(transaction), tenantContextService.getRequiredTenantId());
    }

    public FinancialTransactionResponse discount(UUID id, FinancialAdjustmentRequest request) {
        FinancialTransaction transaction = findTransaction(id);
        ensureMutable(transaction);
        BigDecimal nextDiscount = scale(transaction.getDiscount().add(scale(request.amount())));
        validateTotals(transaction, nextDiscount, transaction.getSurcharge());
        transaction.setDiscount(nextDiscount);
        mergeDescription(transaction, request.description());
        recalculate(transaction);
        return mapToResponse(financialTransactionRepository.save(transaction), tenantContextService.getRequiredTenantId());
    }

    public FinancialTransactionResponse surcharge(UUID id, FinancialAdjustmentRequest request) {
        FinancialTransaction transaction = findTransaction(id);
        ensureMutable(transaction);
        BigDecimal nextSurcharge = scale(transaction.getSurcharge().add(scale(request.amount())));
        validateTotals(transaction, transaction.getDiscount(), nextSurcharge);
        transaction.setSurcharge(nextSurcharge);
        mergeDescription(transaction, request.description());
        recalculate(transaction);
        return mapToResponse(financialTransactionRepository.save(transaction), tenantContextService.getRequiredTenantId());
    }

    public FinancialTransactionResponse cancel(UUID id) {
        FinancialTransaction transaction = findTransaction(id);
        if (transaction.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lançamento pago não pode ser cancelado");
        }
        transaction.setStatus(FinancialStatus.CANCELLED);
        return mapToResponse(financialTransactionRepository.save(transaction), tenantContextService.getRequiredTenantId());
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionResponse> listDue() {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return financialTransactionRepository.findAllByCompanyIdAndDueDateBetweenAndStatusIn(
                        companyId,
                        LocalDate.now(),
                        LocalDate.now(),
                        List.of(FinancialStatus.PENDING, FinancialStatus.PARTIALLY_PAID))
                .stream()
                .map(tx -> mapToResponse(tx, companyId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionResponse> listOverdue() {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return financialTransactionRepository.findAllByCompanyIdAndDueDateBeforeAndStatusIn(
                        companyId,
                        LocalDate.now(),
                        List.of(FinancialStatus.PENDING, FinancialStatus.PARTIALLY_PAID))
                .stream()
                .map(tx -> mapToResponse(tx, companyId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionResponse> listByServiceOrder(UUID serviceOrderId) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return financialTransactionRepository.findByServiceOrderIdAndCompanyId(serviceOrderId, companyId)
                .stream()
                .map(tx -> mapToResponse(tx, companyId))
                .toList();
    }

    public FinancialTransactionResponse generateFromServiceOrder(ServiceOrder serviceOrder) {
        if (!financialProperties.isAutoGenerateOnServiceOrderCompletion()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geração automática desabilitada");
        }
        UUID companyId = tenantContextService.getRequiredTenantId();
        if (financialTransactionRepository.existsByServiceOrderIdAndCompanyId(serviceOrder.getId(), companyId)) {
            return mapToResponse(financialTransactionRepository.findByServiceOrderIdAndCompanyId(serviceOrder.getId(), companyId).orElseThrow(), companyId);
        }

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setCompanyId(companyId);
        transaction.setServiceOrderId(serviceOrder.getId());
        transaction.setCustomerId(serviceOrder.getCustomerId());
        transaction.setProfessionalId(serviceOrder.getProfessionalId());
        transaction.setServiceId(serviceOrder.getServiceId());
        transaction.setTransactionType(TransactionType.RECEIVABLE);
        transaction.setStatus(FinancialStatus.PENDING);
        transaction.setAmount(serviceOrder.getServicePrice());
        transaction.setDiscount(BigDecimal.ZERO);
        transaction.setSurcharge(BigDecimal.ZERO);
        transaction.setPaidAmount(BigDecimal.ZERO);
        transaction.setDueDate(LocalDate.now());
        transaction.setDescription("Lançamento gerado automaticamente da OS " + serviceOrder.getId());
        transaction.setExternalReference(serviceOrder.getId().toString());
        recalculate(transaction);
        FinancialTransaction saved = financialTransactionRepository.save(transaction);
        return mapToResponse(saved, companyId);
    }

    public FinancialTransaction createFromServiceOrder(UUID companyId, UUID serviceOrderId, FinancialTransactionRequest request) {
        ServiceOrder serviceOrder = findServiceOrder(companyId, serviceOrderId);
        if (financialTransactionRepository.existsByServiceOrderIdAndCompanyId(serviceOrderId, companyId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe lançamento financeiro para esta ordem de serviço");
        }
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setCompanyId(companyId);
        transaction.setServiceOrderId(serviceOrder.getId());
        transaction.setCustomerId(serviceOrder.getCustomerId());
        transaction.setProfessionalId(serviceOrder.getProfessionalId());
        transaction.setServiceId(serviceOrder.getServiceId());
        applyRequest(transaction, request, companyId);
        recalculate(transaction);
        return transaction;
    }

    private FinancialTransaction createManual(UUID companyId, FinancialTransactionRequest request) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setCompanyId(companyId);
        applyRequest(transaction, request, companyId);
        recalculate(transaction);
        return transaction;
    }

    private void applyRequest(FinancialTransaction transaction, FinancialTransactionRequest request, UUID companyId) {
        transaction.setAmount(scale(request.amount()));
        transaction.setDiscount(request.discount() == null ? BigDecimal.ZERO : scale(request.discount()));
        transaction.setSurcharge(request.surcharge() == null ? BigDecimal.ZERO : scale(request.surcharge()));
        transaction.setDueDate(request.dueDate());
        transaction.setPaymentMethodId(request.paymentMethodId());
        transaction.setDescription(normalizeText(request.description(), transaction.getDescription()));
        transaction.setExternalReference(normalizeText(request.externalReference(), transaction.getExternalReference()));
        if (request.serviceOrderId() != null) {
            ServiceOrder serviceOrder = findServiceOrder(companyId, request.serviceOrderId());
            transaction.setServiceOrderId(serviceOrder.getId());
            transaction.setCustomerId(serviceOrder.getCustomerId());
            transaction.setProfessionalId(serviceOrder.getProfessionalId());
            transaction.setServiceId(serviceOrder.getServiceId());
        }
        if (transaction.getPaymentMethodId() != null) {
            PaymentMethod paymentMethod = paymentMethodService.resolveActiveMethod(transaction.getPaymentMethodId());
            transaction.setPaymentMethodId(paymentMethod.getId());
        }
    }

    private void recalculate(FinancialTransaction transaction) {
        BigDecimal total = transaction.getAmount()
                .subtract(transaction.getDiscount())
                .add(transaction.getSurcharge());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor total não pode ser negativo");
        }
        if (transaction.getPaidAmount() == null) {
            transaction.setPaidAmount(BigDecimal.ZERO);
        }
        if (transaction.getPaidAmount().compareTo(total) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor pago não pode exceder o total");
        }
        transaction.setTotalAmount(scale(total));
        if (transaction.getPaidAmount().compareTo(BigDecimal.ZERO) > 0 && transaction.getPaidAmount().compareTo(transaction.getTotalAmount()) < 0) {
            transaction.setStatus(FinancialStatus.PARTIALLY_PAID);
        } else if (transaction.getPaidAmount().compareTo(transaction.getTotalAmount()) >= 0 && transaction.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            transaction.setStatus(FinancialStatus.PAID);
        } else if (transaction.getStatus() != FinancialStatus.CANCELLED && transaction.getStatus() != FinancialStatus.REFUNDED) {
            transaction.setStatus(FinancialStatus.PENDING);
        }
        if (transaction.getDueDate() != null
                && transaction.getDueDate().isBefore(LocalDate.now())
                && (transaction.getStatus() == FinancialStatus.PENDING || transaction.getStatus() == FinancialStatus.PARTIALLY_PAID)) {
            transaction.setStatus(FinancialStatus.OVERDUE);
        }
    }

    private void updateStatusAfterPayment(FinancialTransaction transaction) {
        if (transaction.getPaidAmount().compareTo(transaction.getTotalAmount()) >= 0) {
            transaction.setStatus(FinancialStatus.PAID);
        } else {
            transaction.setStatus(FinancialStatus.PARTIALLY_PAID);
        }
    }

    private void validateTotals(FinancialTransaction transaction, BigDecimal discount, BigDecimal surcharge) {
        BigDecimal total = transaction.getAmount().subtract(discount).add(surcharge);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor total não pode ser negativo");
        }
        if (transaction.getPaidAmount() != null && transaction.getPaidAmount().compareTo(total) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor pago não pode exceder o total");
        }
    }

    private void ensureMutable(FinancialTransaction transaction) {
        if (transaction.getStatus() == FinancialStatus.CANCELLED || transaction.getStatus() == FinancialStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lançamento não pode ser alterado");
        }
    }

    private void ensureMutableForPayment(FinancialTransaction transaction) {
        if (transaction.getStatus() == FinancialStatus.CANCELLED || transaction.getStatus() == FinancialStatus.REFUNDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lançamento não pode receber pagamento");
        }
    }

    private FinancialTransaction findTransaction(UUID id) {
        UUID companyId = tenantContextService.getRequiredTenantId();
        return financialTransactionRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento financeiro não encontrado"));
    }

    private ServiceOrder findServiceOrder(UUID companyId, UUID serviceOrderId) {
        return serviceOrderRepository.findByIdAndCompanyId(serviceOrderId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada"));
    }

    private FinancialTransactionResponse mapToResponse(FinancialTransaction transaction, UUID companyId) {
        Map<UUID, Customer> customers = loadCustomers(companyId, singleton(transaction.getCustomerId()));
        Map<UUID, Employee> professionals = loadProfessionals(companyId, singleton(transaction.getProfessionalId()));
        Map<UUID, ServiceOffering> services = loadServices(companyId, singleton(transaction.getServiceId()));
        Map<UUID, PaymentMethod> paymentMethods = loadPaymentMethods(companyId, singleton(transaction.getPaymentMethodId()));
        return financialMapper.toResponse(
                transaction,
                customers.get(transaction.getCustomerId()) == null ? null : customers.get(transaction.getCustomerId()).getNome(),
                professionals.get(transaction.getProfessionalId()) == null ? null : professionals.get(transaction.getProfessionalId()).getName(),
                services.get(transaction.getServiceId()) == null ? null : services.get(transaction.getServiceId()).getName(),
                paymentMethods.get(transaction.getPaymentMethodId()) == null ? null : paymentMethods.get(transaction.getPaymentMethodId()).getName());
    }

    private Specification<FinancialTransaction> buildSpecification(UUID companyId, FinancialTransactionSearchRequest request) {
        return FinancialSpecifications.belongsToCompany(companyId)
                .and(FinancialSpecifications.genericFilter(request.filter()))
                .and(FinancialSpecifications.serviceOrderEquals(request.serviceOrderId()))
                .and(FinancialSpecifications.customerEquals(request.customerId()))
                .and(FinancialSpecifications.professionalEquals(request.professionalId()))
                .and(FinancialSpecifications.serviceEquals(request.serviceId()))
                .and(FinancialSpecifications.paymentMethodEquals(request.paymentMethodId()))
                .and(FinancialSpecifications.transactionTypeEquals(request.transactionType()))
                .and(FinancialSpecifications.statusEquals(request.status()))
                .and(FinancialSpecifications.dueDateFrom(request.dateFrom()))
                .and(FinancialSpecifications.dueDateTo(request.dateTo()));
    }

    private Map<UUID, Customer> loadCustomers(UUID companyId, Collection<UUID> ids) {
        Map<UUID, Customer> values = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return values;
        }
        for (Customer customer : customerRepository.findAllByCompanyIdAndIdIn(companyId, ids)) {
            values.put(customer.getId(), customer);
        }
        return values;
    }

    private Map<UUID, Employee> loadProfessionals(UUID companyId, Collection<UUID> ids) {
        Map<UUID, Employee> values = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return values;
        }
        for (Employee employee : employeeRepository.findAllByCompanyIdAndIdIn(companyId, ids)) {
            values.put(employee.getId(), employee);
        }
        return values;
    }

    private Map<UUID, ServiceOffering> loadServices(UUID companyId, Collection<UUID> ids) {
        Map<UUID, ServiceOffering> values = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return values;
        }
        for (ServiceOffering service : serviceRepository.findAllByCompanyIdAndIdIn(companyId, ids)) {
            values.put(service.getId(), service);
        }
        return values;
    }

    private Map<UUID, PaymentMethod> loadPaymentMethods(UUID companyId, Collection<UUID> ids) {
        Map<UUID, PaymentMethod> values = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return values;
        }
        for (PaymentMethod method : paymentMethodService.list().stream()
                .map(response -> {
                    PaymentMethod method = new PaymentMethod();
                    method.setId(response.id());
                    method.setCompanyId(response.companyId());
                    method.setName(response.name());
                    method.setActive(response.active());
                    return method;
                })
                .filter(method -> ids.contains(method.getId()))
                .toList()) {
            values.put(method.getId(), method);
        }
        return values;
    }

    private String normalizeText(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
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

    private void mergeDescription(FinancialTransaction transaction, String description) {
        if (description != null && !description.isBlank()) {
            transaction.setDescription(description.trim());
        }
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

    private Collection<UUID> singleton(UUID id) {
        return id == null ? Collections.emptyList() : List.of(id);
    }
}
