package br.com.servix.schedule;

import br.com.servix.auth.domain.ProfileName;
import br.com.servix.auth.domain.User;
import br.com.servix.auth.domain.UserStatus;
import br.com.servix.auth.dto.LoginRequest;
import br.com.servix.auth.repository.ProfileRepository;
import br.com.servix.auth.repository.RefreshTokenRepository;
import br.com.servix.auth.repository.UserRepository;
import br.com.servix.company.domain.Company;
import br.com.servix.company.domain.CompanyStatus;
import br.com.servix.company.repository.CompanyRepository;
import br.com.servix.customer.domain.Customer;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.domain.AppointmentStatus;
import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import br.com.servix.schedule.dto.AppointmentRequest;
import br.com.servix.schedule.dto.AppointmentStatusUpdateRequest;
import br.com.servix.schedule.dto.EmployeeRequest;
import br.com.servix.schedule.dto.EmployeeScheduleRequest;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.repository.ServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        appointmentRepository.deleteAll();
        workScheduleRepository.deleteAll();
        employeeRepository.deleteAll();
        customerRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void shouldManageEmployeesAndAppointmentsWithinTenant() throws Exception {
        AuthSeed seed = seedAuthenticatedUser("a");
        String token = login(seed.email(), seed.rawPassword());

        CustomerSeed customer = createCustomer(token, "a");
        ServiceSeed service = createService(token);
        EmployeeSeed employee = createEmployee(token, "a");

        MvcResult createAppointmentResult = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(
                                customer.id(),
                                service.id(),
                                employee.id(),
                                nextMonday(),
                                LocalTime.of(9, 0),
                                "Observações"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.endTime").value("10:30:00"))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createAppointmentResult.getResponse().getContentAsString());
        UUID appointmentId = UUID.fromString(createdJson.path("data").get("id").asText());

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .param("customerId", customer.id().toString())
                        .param("serviceId", service.id().toString())
                        .param("employeeId", employee.id().toString())
                        .param("status", "SCHEDULED")
                        .param("dateFrom", nextMonday().toString())
                        .param("dateTo", nextMonday().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/appointments/day")
                        .header("Authorization", "Bearer " + token)
                        .param("date", nextMonday().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(appointmentId.toString()));

        mockMvc.perform(get("/api/appointments/week")
                        .header("Authorization", "Bearer " + token)
                        .param("date", nextMonday().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(appointmentId.toString()));

        mockMvc.perform(get("/api/appointments/month")
                        .header("Authorization", "Bearer " + token)
                        .param("date", nextMonday().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(appointmentId.toString()));

        mockMvc.perform(get("/api/appointments/employee/" + employee.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(appointmentId.toString()));

        mockMvc.perform(get("/api/appointments/customer/" + customer.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(appointmentId.toString()));

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentStatusUpdateRequest(AppointmentStatus.COMPLETED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        EmployeeRequest updateRequest = new EmployeeRequest(
                "Funcionário Atualizado",
                "funcionario.a@servix.com",
                "11988887777",
                true,
                List.of(new EmployeeScheduleRequest(1, LocalTime.of(8, 0), LocalTime.of(12, 0), true)));

        mockMvc.perform(put("/api/employees/" + employee.id())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Funcionário Atualizado"));

        mockMvc.perform(patch("/api/employees/" + employee.id() + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new br.com.servix.schedule.dto.EmployeeStatusUpdateRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(delete("/api/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockCrossTenantAccessAndConflicts() throws Exception {
        AuthSeed seedA = seedAuthenticatedUser("a");
        AuthSeed seedB = seedAuthenticatedUser("b");

        String tokenA = login(seedA.email(), seedA.rawPassword());
        String tokenB = login(seedB.email(), seedB.rawPassword());

        CustomerSeed customerA = createCustomer(tokenA, "a");
        ServiceSeed serviceA = createService(tokenA);
        EmployeeSeed employeeA = createEmployee(tokenA, "a");

        CustomerSeed customerB = createCustomer(tokenB, "b");
        ServiceSeed serviceB = createService(tokenB);
        EmployeeSeed employeeB = createEmployee(tokenB, "b");

        MvcResult appointmentResult = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(
                                customerB.id(),
                                serviceB.id(),
                                employeeB.id(),
                                nextMonday(),
                                LocalTime.of(9, 0),
                                "Observações"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(appointmentResult.getResponse().getContentAsString());
        UUID appointmentId = UUID.fromString(createdJson.path("data").get("id").asText());

        mockMvc.perform(get("/api/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(
                                customerA.id(),
                                serviceA.id(),
                                employeeA.id(),
                                nextMonday(),
                                LocalTime.of(9, 0),
                                "Outro"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(
                                customerA.id(),
                                serviceA.id(),
                                employeeA.id(),
                                nextMonday(),
                                LocalTime.of(9, 0),
                                "Conflito"))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRequireAuthenticationAndRejectPastAppointment() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());

        AuthSeed seed = seedAuthenticatedUser("c");
        String token = login(seed.email(), seed.rawPassword());
        CustomerSeed customer = createCustomer(token, "c");
        ServiceSeed service = createService(token);
        EmployeeSeed employee = createEmployee(token, "c");

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(
                                customer.id(),
                                service.id(),
                                employee.id(),
                                LocalDate.now().minusDays(1),
                                LocalTime.of(9, 0),
                                "Passado"))))
                .andExpect(status().isBadRequest());
    }

    private CustomerSeed createCustomer(String token, String suffix) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new br.com.servix.customer.dto.CustomerRequest(
                                "Cliente " + suffix,
                                "12345678901",
                                "cliente." + suffix + "@servix.com",
                                "11999999999",
                                null,
                                "01001000",
                                "Rua A",
                                "10",
                                null,
                                "Centro",
                                "São Paulo",
                                "SP",
                                null))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new CustomerSeed(UUID.fromString(json.path("data").get("id").asText()));
    }

    private ServiceSeed createService(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new br.com.servix.service.dto.ServiceRequest(
                                "Limpeza",
                                "Serviço",
                                90,
                                new BigDecimal("150.00")))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new ServiceSeed(UUID.fromString(json.path("data").get("id").asText()));
    }

    private EmployeeSeed createEmployee(String token, String suffix) throws Exception {
        EmployeeRequest request = new EmployeeRequest(
                "Funcionário " + suffix,
                "funcionario." + suffix + "@servix.com",
                "11988887777",
                true,
                List.of(
                        new EmployeeScheduleRequest(1, LocalTime.of(8, 0), LocalTime.of(12, 0), true),
                        new EmployeeScheduleRequest(1, LocalTime.of(13, 0), LocalTime.of(18, 0), true)));

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new EmployeeSeed(UUID.fromString(json.path("data").get("id").asText()));
    }

    private AuthSeed seedAuthenticatedUser(String suffix) {
        Company company = new Company();
        company.setNome("Empresa " + suffix);
        company.setDocumento(String.format("123456780001%02d", Math.abs(suffix.hashCode() % 90) + 10));
        company.setEmail("empresa." + suffix + "@servix.com");
        company.setStatus(CompanyStatus.ACTIVE);
        company = companyRepository.save(company);

        var profile = profileRepository.findByName(ProfileName.ADMIN).orElseThrow();

        String rawPassword = "SenhaForte123";
        User user = new User();
        user.setCompany(company);
        user.setNome("Admin " + suffix);
        user.setEmail("admin." + suffix + "@servix.com");
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setProfiles(Set.of(profile));
        userRepository.save(user);

        return new AuthSeed(user.getEmail(), rawPassword, company.getId());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("data").get("accessToken").asText();
    }

    private LocalDate nextMonday() {
        LocalDate today = LocalDate.now();
        int days = DayOfWeek.MONDAY.getValue() - today.getDayOfWeek().getValue();
        if (days <= 0) {
            days += 7;
        }
        return today.plusDays(days);
    }

    private record AuthSeed(String email, String rawPassword, UUID companyId) {
    }

    private record CustomerSeed(UUID id) {
    }

    private record ServiceSeed(UUID id) {
    }

    private record EmployeeSeed(UUID id) {
    }
}
