package br.com.servix.billing.service;

import br.com.servix.billing.domain.PaymentMethod;
import br.com.servix.billing.dto.PaymentMethodResponse;
import br.com.servix.billing.mapper.FinancialMapper;
import br.com.servix.billing.repository.PaymentMethodRepository;
import br.com.servix.core.tenant.TenantContextService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final FinancialMapper financialMapper;
    private final TenantContextService tenantContextService;

    public List<PaymentMethodResponse> list() {
        return paymentMethodRepository.findAvailable(tenantContextService.getRequiredTenantId())
                .stream()
                .map(financialMapper::toResponse)
                .toList();
    }

    public PaymentMethod resolveActiveMethod(java.util.UUID paymentMethodId) {
        if (paymentMethodId == null) {
            return null;
        }
        return paymentMethodRepository.findActiveById(paymentMethodId, tenantContextService.getRequiredTenantId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Forma de pagamento não encontrada"));
    }
}
