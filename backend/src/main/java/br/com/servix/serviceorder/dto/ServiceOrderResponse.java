package br.com.servix.serviceorder.dto;

import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderResponse(
        UUID id,
        UUID companyId,
        UUID appointmentId,
        UUID customerId,
        String customerName,
        UUID professionalId,
        String professionalName,
        UUID serviceId,
        String serviceName,
        BigDecimal servicePrice,
        Integer estimatedDuration,
        Integer actualDuration,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        ServiceOrderStatus status,
        String observations,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID createdBy,
        UUID updatedBy) {
}
