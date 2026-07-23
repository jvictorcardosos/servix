package br.com.servix.serviceorder;

import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.serviceorder.domain.ServiceOrder;
import br.com.servix.serviceorder.domain.ServiceOrderHistory;
import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import br.com.servix.serviceorder.dto.ServiceOrderRequest;
import br.com.servix.serviceorder.mapper.ServiceOrderMapper;
import br.com.servix.serviceorder.repository.ServiceOrderHistoryRepository;
import br.com.servix.serviceorder.repository.ServiceOrderRepository;
import br.com.servix.serviceorder.service.ServiceOrderService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private ServiceOrderHistoryRepository serviceOrderHistoryRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceOrderMapper serviceOrderMapper;

    @Mock
    private TenantContextService tenantContextService;

    private ServiceOrderService serviceOrderService;

    @BeforeEach
    void setUp() {
        serviceOrderService = new ServiceOrderService(
                serviceOrderRepository,
                serviceOrderHistoryRepository,
                appointmentRepository,
                customerRepository,
                employeeRepository,
                serviceRepository,
                serviceOrderMapper,
                tenantContextService);
    }

    @Test
    void shouldRejectFinishWithoutStart() {
        UUID companyId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        ServiceOrder order = new ServiceOrder();
        order.setId(orderId);
        order.setCompanyId(companyId);
        order.setStatus(ServiceOrderStatus.OPEN);

        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(serviceOrderRepository.findByIdAndCompanyId(orderId, companyId)).thenReturn(java.util.Optional.of(order));

        assertThatThrownBy(() -> serviceOrderService.finish(orderId))
                .isInstanceOf(ResponseStatusException.class);

        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
    }

    @Test
    void shouldRejectStartingAnotherActiveOrderForSameProfessional() {
        UUID companyId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID professionalId = UUID.randomUUID();
        ServiceOrder order = new ServiceOrder();
        order.setId(orderId);
        order.setCompanyId(companyId);
        order.setProfessionalId(professionalId);
        order.setStatus(ServiceOrderStatus.OPEN);
        order.setObservations("Observação");

        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(serviceOrderRepository.findByIdAndCompanyId(orderId, companyId)).thenReturn(java.util.Optional.of(order));
        when(serviceOrderRepository.existsByCompanyIdAndProfessionalIdAndStatusAndIdNot(companyId, professionalId, ServiceOrderStatus.IN_PROGRESS, orderId))
                .thenReturn(true);

        assertThatThrownBy(() -> serviceOrderService.start(orderId))
                .isInstanceOf(ResponseStatusException.class);

        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
        verify(serviceOrderHistoryRepository, never()).save(any(ServiceOrderHistory.class));
    }
}
