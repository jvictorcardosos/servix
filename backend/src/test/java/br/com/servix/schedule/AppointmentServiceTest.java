package br.com.servix.schedule;

import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.config.ScheduleProperties;
import br.com.servix.schedule.domain.Appointment;
import br.com.servix.schedule.domain.AppointmentStatus;
import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.domain.WorkSchedule;
import br.com.servix.schedule.dto.AppointmentRequest;
import br.com.servix.schedule.mapper.AppointmentMapper;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import br.com.servix.schedule.service.AppointmentService;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.repository.ServiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private TenantContextService tenantContextService;

    @Mock
    private ScheduleProperties scheduleProperties;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                employeeRepository,
                workScheduleRepository,
                customerRepository,
                serviceRepository,
                appointmentMapper,
                tenantContextService,
                scheduleProperties);
    }

    @Test
    void shouldRejectPastAppointmentWhenNotAllowed() {
        UUID companyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();

        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(scheduleProperties.isAllowPastAppointments()).thenReturn(false);
        when(scheduleProperties.getPastAppointmentToleranceMinutes()).thenReturn(0);
        when(customerRepository.findByIdAndCompanyId(customerId, companyId)).thenReturn(java.util.Optional.of(activeCustomer(customerId)));
        when(serviceRepository.findByIdAndCompanyId(serviceId, companyId)).thenReturn(java.util.Optional.of(activeService(serviceId, 90)));
        when(employeeRepository.findByIdAndCompanyId(employeeId, companyId)).thenReturn(java.util.Optional.of(activeEmployee(employeeId)));

        AppointmentRequest request = new AppointmentRequest(
                customerId,
                serviceId,
                employeeId,
                LocalDate.now().minusDays(1),
                LocalTime.of(9, 0),
                "Observações");

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(ResponseStatusException.class);

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldRejectEmployeeConflict() {
        UUID companyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        LocalDate appointmentDate = LocalDate.now().plusDays(1);

        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(scheduleProperties.isAllowPastAppointments()).thenReturn(true);
        when(customerRepository.findByIdAndCompanyId(customerId, companyId)).thenReturn(java.util.Optional.of(activeCustomer(customerId)));
        when(serviceRepository.findByIdAndCompanyId(serviceId, companyId)).thenReturn(java.util.Optional.of(activeService(serviceId, 60)));
        when(employeeRepository.findByIdAndCompanyId(employeeId, companyId)).thenReturn(java.util.Optional.of(activeEmployee(employeeId)));
        when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrueOrderByStartTimeAsc(employeeId, appointmentDate.getDayOfWeek().getValue()))
                .thenReturn(List.of(activeSchedule(employeeId, appointmentDate.getDayOfWeek().getValue(), LocalTime.of(8, 0), LocalTime.of(12, 0))));
        when(appointmentRepository.existsEmployeeConflict(companyId, employeeId, appointmentDate, LocalTime.of(9, 0), LocalTime.of(10, 0), List.of(
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.IN_PROGRESS), null)).thenReturn(true);

        AppointmentRequest request = new AppointmentRequest(
                customerId,
                serviceId,
                employeeId,
                appointmentDate,
                LocalTime.of(9, 0),
                "Observações");

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(ResponseStatusException.class);

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    private Customer activeCustomer(UUID id) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setNome("Cliente");
        customer.setCpfCnpj("12345678901");
        customer.setEmail("cliente@servix.com");
        customer.setTelefone("11999999999");
        customer.setCep("01001000");
        customer.setLogradouro("Rua A");
        customer.setNumero("10");
        customer.setBairro("Centro");
        customer.setCidade("São Paulo");
        customer.setEstado("SP");
        customer.setAtivo(true);
        return customer;
    }

    private ServiceOffering activeService(UUID id, int durationMinutes) {
        ServiceOffering service = new ServiceOffering();
        service.setId(id);
        service.setName("Serviço");
        service.setDescription("Descrição");
        service.setDurationMinutes(durationMinutes);
        service.setPrice(new BigDecimal("100.00"));
        service.setActive(true);
        return service;
    }

    private Employee activeEmployee(UUID id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName("Funcionário");
        employee.setEmail("funcionario@servix.com");
        employee.setPhone("11999999999");
        employee.setActive(true);
        return employee;
    }

    private WorkSchedule activeSchedule(UUID employeeId, Integer dayOfWeek, LocalTime startTime, LocalTime endTime) {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setEmployeeId(employeeId);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setActive(true);
        return schedule;
    }
}
