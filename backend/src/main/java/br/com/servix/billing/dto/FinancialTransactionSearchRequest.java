package br.com.servix.billing.dto;

import br.com.servix.billing.domain.FinancialStatus;
import br.com.servix.billing.domain.TransactionType;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialTransactionSearchRequest(
        Integer page,
        Integer size,
        String sortBy,
        String direction,
        String filter,
        UUID serviceOrderId,
        UUID customerId,
        UUID professionalId,
        UUID serviceId,
        UUID paymentMethodId,
        TransactionType transactionType,
        FinancialStatus status,
        LocalDate dateFrom,
        LocalDate dateTo) {
}
