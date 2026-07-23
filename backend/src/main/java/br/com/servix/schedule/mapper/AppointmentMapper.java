package br.com.servix.schedule.mapper;

import br.com.servix.schedule.domain.Appointment;
import br.com.servix.schedule.dto.AppointmentResponse;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(
            Appointment appointment,
            String customerName,
            String serviceName,
            String employeeName) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getCompanyId(),
                appointment.getCustomerId(),
                customerName,
                appointment.getServiceId(),
                serviceName,
                appointment.getEmployeeId(),
                employeeName,
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                (int) ChronoUnit.MINUTES.between(appointment.getStartTime(), appointment.getEndTime()),
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                appointment.getCreatedBy(),
                appointment.getUpdatedBy());
    }
}
