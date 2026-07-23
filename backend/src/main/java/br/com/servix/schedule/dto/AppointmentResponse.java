package br.com.servix.schedule.dto;

import br.com.servix.schedule.domain.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID companyId,
        UUID customerId,
        String customerName,
        UUID serviceId,
        String serviceName,
        UUID employeeId,
        String employeeName,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer durationMinutes,
        AppointmentStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID createdBy,
        UUID updatedBy) {
}
