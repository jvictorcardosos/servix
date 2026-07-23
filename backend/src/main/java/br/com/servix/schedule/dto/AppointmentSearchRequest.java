package br.com.servix.schedule.dto;

import br.com.servix.schedule.domain.AppointmentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AppointmentSearchRequest(
        Integer page,
        Integer size,
        String sortBy,
        String direction,
        String filter,
        UUID customerId,
        UUID serviceId,
        UUID employeeId,
        AppointmentStatus status,
        LocalDate dateFrom,
        LocalDate dateTo) {
}
