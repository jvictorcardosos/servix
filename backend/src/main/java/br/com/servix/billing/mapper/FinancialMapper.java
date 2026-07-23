package br.com.servix.billing.mapper;

import br.com.servix.billing.domain.FinancialTransaction;
import br.com.servix.billing.domain.PaymentMethod;
import br.com.servix.billing.dto.FinancialTransactionResponse;
import br.com.servix.billing.dto.PaymentMethodResponse;
import org.springframework.stereotype.Component;

@Component
public class FinancialMapper {

    public FinancialTransactionResponse toResponse(
            FinancialTransaction transaction,
            String customerName,
            String professionalName,
            String serviceName,
            String paymentMethodName) {
        return new FinancialTransactionResponse(
                transaction.getId(),
                transaction.getCompanyId(),
                transaction.getServiceOrderId(),
                transaction.getCustomerId(),
                customerName,
                transaction.getProfessionalId(),
                professionalName,
                transaction.getServiceId(),
                serviceName,
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getDiscount(),
                transaction.getSurcharge(),
                transaction.getTotalAmount(),
                transaction.getPaidAmount(),
                transaction.getTotalAmount().subtract(transaction.getPaidAmount()),
                transaction.getDueDate(),
                transaction.getPaymentDate(),
                transaction.getPaymentMethodId(),
                paymentMethodName,
                transaction.getDescription(),
                transaction.getExternalReference(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt(),
                transaction.getCreatedBy(),
                transaction.getUpdatedBy());
    }

    public PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.getId(),
                paymentMethod.getCompanyId(),
                paymentMethod.getName(),
                paymentMethod.isActive());
    }
}
