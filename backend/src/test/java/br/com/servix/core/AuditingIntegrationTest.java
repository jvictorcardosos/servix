package br.com.servix.core;

import br.com.servix.auth.repository.RefreshTokenRepository;
import br.com.servix.auth.repository.UserRepository;
import br.com.servix.company.domain.Company;
import br.com.servix.company.domain.CompanyStatus;
import br.com.servix.company.repository.CompanyRepository;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.billing.repository.FinancialTransactionRepository;
import br.com.servix.serviceorder.repository.ServiceOrderHistoryRepository;
import br.com.servix.serviceorder.repository.ServiceOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuditingIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private ServiceOrderHistoryRepository serviceOrderHistoryRepository;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @BeforeEach
    void setup() {
        refreshTokenRepository.deleteAll();
        financialTransactionRepository.deleteAll();
        serviceOrderHistoryRepository.deleteAll();
        serviceOrderRepository.deleteAll();
        appointmentRepository.deleteAll();
        workScheduleRepository.deleteAll();
        employeeRepository.deleteAll();
        customerRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void shouldFillAuditTimestampsAutomatically() {
        Company company = new Company();
        company.setNome("Empresa Audit");
        company.setDocumento("55555555000101");
        company.setEmail("audit@servix.com");
        company.setStatus(CompanyStatus.ACTIVE);

        Company saved = companyRepository.save(company);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
