package br.com.servix.customer;

import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.dto.CustomerRequest;
import br.com.servix.customer.mapper.CustomerMapper;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.customer.service.CustomerService;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private TenantContextService tenantContextService;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, customerMapper, tenantContextService);
    }

    @Test
    void shouldNormalizeCustomerDataBeforeSaving() {
        UUID companyId = UUID.randomUUID();
        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(customerMapper.toEntity(any(CustomerRequest.class))).thenAnswer(invocation -> new Customer());
        when(customerRepository.existsByCompanyIdAndCpfCnpj(companyId, "12345678000199")).thenReturn(false);
        when(customerRepository.existsByCompanyIdAndEmail(companyId, "cliente@servix.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerRequest request = new CustomerRequest(
                "  Cliente Teste  ",
                "12.345.678/0001-99",
                " Cliente@Servix.com ",
                "(11) 99999-8888",
                "(11) 98888-7777",
                "12.345-678",
                " Rua A ",
                " 10 ",
                " Sala 1 ",
                " Centro ",
                " Sao Paulo ",
                "sp",
                " Observações ");

        customerService.create(request);

        ArgumentCaptor<CustomerRequest> requestCaptor = ArgumentCaptor.forClass(CustomerRequest.class);
        verify(customerMapper).toEntity(requestCaptor.capture());
        CustomerRequest normalized = requestCaptor.getValue();

        assertThat(normalized.nome()).isEqualTo("Cliente Teste");
        assertThat(normalized.cpfCnpj()).isEqualTo("12345678000199");
        assertThat(normalized.email()).isEqualTo("cliente@servix.com");
        assertThat(normalized.telefone()).isEqualTo("11999998888");
        assertThat(normalized.telefoneSecundario()).isEqualTo("11988887777");
        assertThat(normalized.cep()).isEqualTo("12345678");
        assertThat(normalized.estado()).isEqualTo("SP");
    }

    @Test
    void shouldRejectDuplicatedCustomerDocumentInTenant() {
        UUID companyId = UUID.randomUUID();
        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(customerMapper.toEntity(any(CustomerRequest.class))).thenAnswer(invocation -> new Customer());
        when(customerRepository.existsByCompanyIdAndCpfCnpj(companyId, "12345678000199")).thenReturn(true);

        CustomerRequest request = new CustomerRequest(
                "Cliente",
                "12.345.678/0001-99",
                "cliente@servix.com",
                "11999998888",
                null,
                "12345678",
                "Rua A",
                "10",
                null,
                "Centro",
                "São Paulo",
                "SP",
                null);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(ResponseStatusException.class);

        verify(customerRepository, never()).save(any());
    }
}
