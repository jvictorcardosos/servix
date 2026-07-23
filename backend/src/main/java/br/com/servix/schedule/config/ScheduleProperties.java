package br.com.servix.schedule.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "servix.schedule")
public class ScheduleProperties {

    private boolean allowPastAppointments;
    private int pastAppointmentToleranceMinutes;

    public boolean isAllowPastAppointments() {
        return allowPastAppointments;
    }

    public void setAllowPastAppointments(boolean allowPastAppointments) {
        this.allowPastAppointments = allowPastAppointments;
    }

    public int getPastAppointmentToleranceMinutes() {
        return pastAppointmentToleranceMinutes;
    }

    public void setPastAppointmentToleranceMinutes(int pastAppointmentToleranceMinutes) {
        this.pastAppointmentToleranceMinutes = pastAppointmentToleranceMinutes;
    }
}
