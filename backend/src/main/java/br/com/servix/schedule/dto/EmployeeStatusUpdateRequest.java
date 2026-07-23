package br.com.servix.schedule.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.NotNull;

public record EmployeeStatusUpdateRequest(
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        Boolean active) {
}
