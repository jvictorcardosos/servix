package br.com.servix.serviceorder.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderRequest(
        UUID appointmentId,
        UUID customerId,
        UUID professionalId,
        UUID serviceId,
        LocalDateTime scheduledStart,
        @Size(max = 4000, message = ValidationMessages.MAX_LENGTH)
        String observations) {
}
