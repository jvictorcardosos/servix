package br.com.servix.billing;

import br.com.servix.billing.config.FinancialProperties;
import br.com.servix.billing.domain.FinancialStatus;
import br.com.servix.billing.domain.FinancialTransaction;
import br.com.servix.billing.domain.PaymentMethod;
import br.com.servix.billing.domain.TransactionType;
import br.com.servix.billing.dto.PaymentMethodResponse;
import br.com.servix.billing.dto.FinancialPaymentRequest;
import br.com.servix.billing.dto.FinancialTransactionResponse;
import br.com.servix.billing.dto.FinancialTransactionRequest;
import br.com.servix.billing.mapper.FinancialMapper;
import br.com.servix.billing.repository.FinancialTransactionRepository;
import br.com.servix.billing.service.FinancialService;
import br.com.servix.billing.service.PaymentMethodService;
import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.serviceorder.domain.ServiceOrder;
import br.com.servix.serviceorder.repository.ServiceOrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialServiceTest {

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private PaymentMethodService paymentMethodService;

    private FinancialMapper financialMapper;

    @Mock
    private TenantContextService tenantContextService;

    @Mock
    private FinancialProperties financialProperties;

    private FinancialService financialService;

    @BeforeEach
    void setUp() {
        financialService = new FinancialService(
                financialTransactionRepository,
                serviceOrderRepository,
                customerRepository,
                employeeRepository,
                serviceRepository,
                paymentMethodService,
                new FinancialMapper(),
                tenantContextService,
                financialProperties);
    }

    @Test
    void shouldRegisterPartialPayment() {
        UUID companyId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setId(transactionId);
        transaction.setCompanyId(companyId);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setDiscount(BigDecimal.ZERO);
        transaction.setSurcharge(BigDecimal.ZERO);
        transaction.setTotalAmount(new BigDecimal("100.00"));
        transaction.setPaidAmount(BigDecimal.ZERO);
        transaction.setStatus(FinancialStatus.PENDING);
        transaction.setDueDate(LocalDate.now().plusDays(5));
        transaction.setTransactionType(TransactionType.RECEIVABLE);

        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(financialTransactionRepository.findByIdAndCompanyId(transactionId, companyId)).thenReturn(Optional.of(transaction));
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(paymentMethodId);
        paymentMethod.setName("PIX");
        paymentMethod.setActive(true);
        when(paymentMethodService.list()).thenReturn(List.of(new PaymentMethodResponse(paymentMethodId, null, "PIX", true)));
        when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialTransactionResponse updated = financialService.pay(
                transactionId,
                new FinancialPaymentRequest(new BigDecimal("40.00"), paymentMethodId, LocalDate.now(), "REF-1", "Parcial"));

        assertThat(updated.paidAmount()).isEqualByComparingTo("40.00");
        assertThat(updated.status()).isEqualTo(FinancialStatus.PARTIALLY_PAID);
        verify(financialTransactionRepository).save(any(FinancialTransaction.class));
    }

    @Test
    void shouldGenerateFinancialTransactionFromServiceOrder() {
        UUID companyId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        ServiceOrder order = new ServiceOrder();
        order.setId(orderId);
        order.setCompanyId(companyId);
        order.setCustomerId(UUID.randomUUID());
        order.setProfessionalId(UUID.randomUUID());
        order.setServiceId(UUID.randomUUID());
        order.setServicePrice(new BigDecimal("150.00"));

        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(financialProperties.isAutoGenerateOnServiceOrderCompletion()).thenReturn(true);
        when(financialTransactionRepository.existsByServiceOrderIdAndCompanyId(orderId, companyId)).thenReturn(false);
        when(financialTransactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialTransactionResponse generated = financialService.generateFromServiceOrder(order);

        assertThat(generated.serviceOrderId()).isEqualTo(orderId);
        assertThat(generated.status()).isEqualTo(FinancialStatus.PENDING);
        assertThat(generated.totalAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldRejectDiscountThatMakesTotalNegative() {
        UUID companyId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setId(transactionId);
        transaction.setCompanyId(companyId);
        transaction.setAmount(new BigDecimal("10.00"));
        transaction.setDiscount(BigDecimal.ZERO);
        transaction.setSurcharge(BigDecimal.ZERO);
        transaction.setTotalAmount(new BigDecimal("10.00"));
        transaction.setPaidAmount(BigDecimal.ZERO);
        transaction.setStatus(FinancialStatus.PENDING);
        transaction.setDueDate(LocalDate.now().plusDays(1));

        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(financialTransactionRepository.findByIdAndCompanyId(transactionId, companyId)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> financialService.discount(transactionId, new br.com.servix.billing.dto.FinancialAdjustmentRequest(new BigDecimal("20.00"), "Erro")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }
}
