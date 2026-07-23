package br.com.servix.schedule.mapper;

import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.domain.WorkSchedule;
import br.com.servix.schedule.dto.EmployeeRequest;
import br.com.servix.schedule.dto.EmployeeResponse;
import br.com.servix.schedule.dto.EmployeeScheduleRequest;
import br.com.servix.schedule.dto.EmployeeScheduleResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        apply(request, employee);
        return employee;
    }

    public void apply(EmployeeRequest request, Employee employee) {
        employee.setName(request.name().trim());
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setPhone(request.phone() == null ? null : request.phone().trim());
        employee.setActive(Boolean.TRUE.equals(request.active()));
    }

    public EmployeeResponse toResponse(Employee employee, List<WorkSchedule> schedules) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getCompanyId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.isActive(),
                schedules.stream().map(this::toResponse).toList(),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                employee.getCreatedBy(),
                employee.getUpdatedBy());
    }

    public EmployeeResponse toSummaryResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getCompanyId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.isActive(),
                List.of(),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                employee.getCreatedBy(),
                employee.getUpdatedBy());
    }

    public WorkSchedule toScheduleEntity(EmployeeScheduleRequest request, Employee employee) {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setEmployeeId(employee.getId());
        apply(request, schedule);
        return schedule;
    }

    public void apply(EmployeeScheduleRequest request, WorkSchedule schedule) {
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setActive(Boolean.TRUE.equals(request.active()));
    }

    public EmployeeScheduleResponse toResponse(WorkSchedule schedule) {
        return new EmployeeScheduleResponse(
                schedule.getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.isActive());
    }
}
