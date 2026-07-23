package br.com.servix.schedule.repository;

import br.com.servix.schedule.domain.WorkSchedule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {

    List<WorkSchedule> findByEmployeeIdOrderByDayOfWeekAscStartTimeAsc(UUID employeeId);

    List<WorkSchedule> findByEmployeeIdAndDayOfWeekAndActiveTrueOrderByStartTimeAsc(UUID employeeId, Integer dayOfWeek);

    void deleteByEmployeeId(UUID employeeId);
}
