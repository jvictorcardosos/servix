package br.com.servix.schedule.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record EmployeeScheduleRequest(
        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        Integer dayOfWeek,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        LocalTime startTime,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        LocalTime endTime,

        Boolean active) {
}
