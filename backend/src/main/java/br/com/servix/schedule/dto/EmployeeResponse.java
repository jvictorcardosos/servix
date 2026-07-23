package br.com.servix.schedule.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        UUID companyId,
        String name,
        String email,
        String phone,
        boolean active,
        List<EmployeeScheduleResponse> workSchedules,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID createdBy,
        UUID updatedBy) {
}
