package br.com.servix.schedule.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentRequest(
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        UUID customerId,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        UUID serviceId,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        UUID employeeId,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        LocalDate appointmentDate,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        LocalTime startTime,

        @Size(max = 4000, message = ValidationMessages.MAX_LENGTH)
        String notes) {
}
