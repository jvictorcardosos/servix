package br.com.servix.schedule.dto;

import br.com.servix.core.validation.ValidationMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EmployeeRequest(
        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Size(max = 150, message = ValidationMessages.MAX_LENGTH)
        String name,

        @NotBlank(message = ValidationMessages.REQUIRED_FIELD)
        @Email(message = ValidationMessages.INVALID_EMAIL)
        @Size(max = 150, message = ValidationMessages.MAX_LENGTH)
        String email,

        @Size(max = 20, message = ValidationMessages.MAX_LENGTH)
        String phone,

        @NotNull(message = ValidationMessages.REQUIRED_FIELD)
        Boolean active,

        @NotEmpty(message = ValidationMessages.REQUIRED_FIELD)
        List<@Valid EmployeeScheduleRequest> workSchedules) {
}
