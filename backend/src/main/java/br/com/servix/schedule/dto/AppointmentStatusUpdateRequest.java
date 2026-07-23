package br.com.servix.schedule.dto;

import br.com.servix.core.validation.ValidationMessages;
import br.com.servix.schedule.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateRequest(
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        AppointmentStatus status) {
}
