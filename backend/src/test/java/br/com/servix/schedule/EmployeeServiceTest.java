package br.com.servix.schedule;

import br.com.servix.core.tenant.TenantContextService;
import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.dto.EmployeeRequest;
import br.com.servix.schedule.dto.EmployeeScheduleRequest;
import br.com.servix.schedule.mapper.EmployeeMapper;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import br.com.servix.schedule.service.EmployeeService;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private TenantContextService tenantContextService;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, workScheduleRepository, employeeMapper, tenantContextService);
    }

    @Test
    void shouldRejectOverlappingWorkSchedules() {
        UUID companyId = UUID.randomUUID();
        when(tenantContextService.getRequiredTenantId()).thenReturn(companyId);
        when(employeeRepository.existsByCompanyIdAndEmail(companyId, "funcionario@servix.com")).thenReturn(false);
        when(employeeMapper.toEntity(any(EmployeeRequest.class))).thenReturn(new Employee());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeRequest request = new EmployeeRequest(
                "Funcionário",
                "funcionario@servix.com",
                "11999999999",
                true,
                List.of(
                        new EmployeeScheduleRequest(1, LocalTime.of(8, 0), LocalTime.of(12, 0), true),
                        new EmployeeScheduleRequest(1, LocalTime.of(11, 0), LocalTime.of(17, 0), true)));

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(ResponseStatusException.class);
    }
}
