package br.com.servix.serviceorder.dto;

import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderHistoryResponse(
        UUID id,
        UUID serviceOrderId,
        ServiceOrderStatus previousStatus,
        ServiceOrderStatus newStatus,
        UUID changedBy,
        LocalDateTime changedAt,
        String observation) {
}
