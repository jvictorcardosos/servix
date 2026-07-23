package br.com.servix.schedule.dto;

import java.time.LocalTime;
import java.util.UUID;

public record EmployeeScheduleResponse(
        UUID id,
        Integer dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active) {
}
