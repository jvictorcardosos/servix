package br.com.servix.service;

import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.dto.ServiceRequest;
import br.com.servix.service.mapper.ServiceMapper;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.service.service.ServiceService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceMapper serviceMapper;

    @Mock
    private TenantContextService tenantContextService;

    private ServiceService serviceService;

    @BeforeEach
    void setUp() {
        serviceService = new ServiceService(serviceRepository, serviceMapper, tenantContextService);
    }

    @Test
    void shouldNormalizeServiceBeforeSaving() {
        UUID companyId = UUID.randomUUID();
        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(serviceMapper.toEntity(any(ServiceRequest.class))).thenAnswer(invocation -> new ServiceOffering());
        when(serviceRepository.existsByCompanyIdAndName(companyId, "Limpeza")).thenReturn(false);
        when(serviceRepository.save(any(ServiceOffering.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serviceService.create(new ServiceRequest("  Limpeza  ", " Geral ", 90, new BigDecimal("99.9")));

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceMapper).toEntity(captor.capture());
        ServiceRequest normalized = captor.getValue();

        assertThat(normalized.name()).isEqualTo("Limpeza");
        assertThat(normalized.description()).isEqualTo("Geral");
        assertThat(normalized.durationMinutes()).isEqualTo(90);
        assertThat(normalized.price()).isEqualByComparingTo("99.90");
    }

    @Test
    void shouldRejectDuplicatedServiceNameInTenant() {
        UUID companyId = UUID.randomUUID();
        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(serviceRepository.existsByCompanyIdAndName(companyId, "Limpeza")).thenReturn(true);

        ServiceRequest request = new ServiceRequest("Limpeza", "Geral", 60, new BigDecimal("100.00"));

        assertThatThrownBy(() -> serviceService.create(request))
                .isInstanceOf(ResponseStatusException.class);

        verify(serviceRepository, never()).save(any());
    }
}
