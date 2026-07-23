package br.com.servix.billing.dto;

import br.com.servix.billing.domain.FinancialStatus;
import br.com.servix.billing.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialTransactionResponse(
        UUID id,
        UUID companyId,
        UUID serviceOrderId,
        UUID customerId,
        String customerName,
        UUID professionalId,
        String professionalName,
        UUID serviceId,
        String serviceName,
        TransactionType transactionType,
        FinancialStatus status,
        BigDecimal amount,
        BigDecimal discount,
        BigDecimal surcharge,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        LocalDate dueDate,
        LocalDate paymentDate,
        UUID paymentMethodId,
        String paymentMethodName,
        String description,
        String externalReference,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID createdBy,
        UUID updatedBy) {
}
