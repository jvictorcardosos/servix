package br.com.servix.schedule.repository;

import br.com.servix.schedule.domain.Appointment;
import br.com.servix.schedule.domain.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {

    Optional<Appointment> findByIdAndCompanyId(UUID id, UUID companyId);

    List<Appointment> findAllByCompanyIdAndAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(
            UUID companyId,
            LocalDate from,
            LocalDate to);

    List<Appointment> findAllByCompanyIdAndEmployeeIdAndAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(
            UUID companyId,
            UUID employeeId,
            LocalDate from,
            LocalDate to);

    List<Appointment> findAllByCompanyIdAndCustomerIdAndAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(
            UUID companyId,
            UUID customerId,
            LocalDate from,
            LocalDate to);

    @Query("""
            select case when count(a) > 0 then true else false end
            from Appointment a
            where a.companyId = :companyId
              and a.employeeId = :employeeId
              and a.appointmentDate = :appointmentDate
              and a.startTime < :endTime
              and a.endTime > :startTime
              and a.status in :statuses
              and (:ignoreId is null or a.id <> :ignoreId)
            """)
    boolean existsEmployeeConflict(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            @Param("ignoreId") UUID ignoreId);

    @Query("""
            select case when count(a) > 0 then true else false end
            from Appointment a
            where a.companyId = :companyId
              and a.customerId = :customerId
              and a.appointmentDate = :appointmentDate
              and a.startTime < :endTime
              and a.endTime > :startTime
              and a.status in :statuses
              and (:ignoreId is null or a.id <> :ignoreId)
            """)
    boolean existsCustomerConflict(
            @Param("companyId") UUID companyId,
            @Param("customerId") UUID customerId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            @Param("ignoreId") UUID ignoreId);
}
