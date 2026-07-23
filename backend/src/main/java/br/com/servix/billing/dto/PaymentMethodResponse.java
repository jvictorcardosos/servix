package br.com.servix.billing.dto;

import java.util.UUID;

public record PaymentMethodResponse(
        UUID id,
        UUID companyId,
        String name,
        boolean active) {
}
