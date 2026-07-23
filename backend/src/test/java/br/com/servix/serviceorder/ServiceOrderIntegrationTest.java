package br.com.servix.serviceorder;

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
import br.com.servix.schedule.dto.EmployeeRequest;
import br.com.servix.schedule.dto.EmployeeScheduleRequest;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import br.com.servix.serviceorder.dto.ServiceOrderRequest;
import br.com.servix.serviceorder.repository.ServiceOrderHistoryRepository;
import br.com.servix.serviceorder.repository.ServiceOrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceOrderIntegrationTest {

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
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private ServiceOrderHistoryRepository serviceOrderHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
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
    void shouldManageServiceOrdersWithinTenantAndTrackHistory() throws Exception {
        AuthSeed seed = seedAuthenticatedUser("a");
        String token = login(seed.email(), seed.rawPassword());

        CustomerSeed customer1 = createCustomer(token, "a");
        CustomerSeed customer2 = createCustomer(token, "b");
        ServiceSeed service = createService(token);
        EmployeeSeed employee = createEmployee(token, "a");

        ServiceOrderSeed order1 = createManualOrder(token, customer1.id(), service.id(), employee.id(), LocalDateTime.now().plusDays(2).withHour(9).withMinute(0), "Primeira OS");
        ServiceOrderSeed order2 = createManualOrder(token, customer2.id(), service.id(), employee.id(), LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), "Segunda OS");

        mockMvc.perform(patch("/api/service-orders/" + order1.id() + "/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/service-orders/" + order2.id() + "/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/service-orders/" + order1.id() + "/pause")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"));

        mockMvc.perform(patch("/api/service-orders/" + order1.id() + "/resume")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/service-orders/" + order1.id() + "/finish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.actualDuration").isNumber());

        mockMvc.perform(get("/api/service-orders/history/" + order1.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].newStatus").value("OPEN"))
                .andExpect(jsonPath("$.data[4].newStatus").value("COMPLETED"));

        mockMvc.perform(delete("/api/service-orders/" + order1.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        MvcResult appointmentResult = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AppointmentRequest(
                                customer1.id(),
                                service.id(),
                                employee.id(),
                                nextMonday(),
                                LocalTime.of(10, 0),
                                "Agendamento base"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode appointmentJson = objectMapper.readTree(appointmentResult.getResponse().getContentAsString());
        UUID appointmentId = UUID.fromString(appointmentJson.path("data").get("id").asText());

        MvcResult orderFromAppointmentResult = mockMvc.perform(post("/api/service-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceOrderRequest(
                                appointmentId,
                                null,
                                null,
                                null,
                                null,
                                "Criada a partir da agenda"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andReturn();

        JsonNode serviceOrderJson = objectMapper.readTree(orderFromAppointmentResult.getResponse().getContentAsString());
        UUID orderFromAppointmentId = UUID.fromString(serviceOrderJson.path("data").get("id").asText());

        mockMvc.perform(get("/api/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        mockMvc.perform(get("/api/service-orders/status/COMPLETED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        AuthSeed seedB = seedAuthenticatedUser("b");
        String tokenB = login(seedB.email(), seedB.rawPassword());
        CustomerSeed customerB = createCustomer(tokenB, "c");
        ServiceSeed serviceB = createService(tokenB);
        EmployeeSeed employeeB = createEmployee(tokenB, "b");
        ServiceOrderSeed orderB = createManualOrder(tokenB, customerB.id(), serviceB.id(), employeeB.id(), LocalDateTime.now().plusDays(3).withHour(9).withMinute(0), "Tenant B");

        mockMvc.perform(get("/api/service-orders/" + orderB.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        assertThat(serviceOrderRepository.findByIdAndCompanyId(orderFromAppointmentId, seed.companyId())).isPresent();
    }

    @Test
    void shouldRequireAuthenticationForServiceOrderEndpoints() throws Exception {
        mockMvc.perform(get("/api/service-orders"))
                .andExpect(status().isUnauthorized());
    }

    private CustomerSeed createCustomer(String token, String suffix) throws Exception {
        String document = String.format("12345678%03d", Math.abs(suffix.hashCode() % 900) + 100);
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new br.com.servix.customer.dto.CustomerRequest(
                                "Cliente " + suffix,
                                document,
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

    private ServiceOrderSeed createManualOrder(
            String token,
            UUID customerId,
            UUID serviceId,
            UUID professionalId,
            LocalDateTime scheduledStart,
            String observations) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/service-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceOrderRequest(
                                null,
                                customerId,
                                professionalId,
                                serviceId,
                                scheduledStart,
                                observations))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new ServiceOrderSeed(UUID.fromString(json.path("data").get("id").asText()));
    }

    private AuthSeed seedAuthenticatedUser(String suffix) {
        Company company = new Company();
        company.setNome("Empresa " + suffix);
        company.setDocumento(String.format("123456780001%02d", Math.abs(suffix.hashCode() % 90) + 10));
        company.setEmail("empresa." + suffix + "@servix.com");
        company.setStatus(CompanyStatus.ACTIVE);
        company = companyRepository.save(company);

        User user = new User();
        user.setCompany(company);
        user.setNome("Admin " + suffix);
        user.setEmail("admin." + suffix + "@servix.com");
        user.setPasswordHash(passwordEncoder.encode("SenhaForte123"));
        user.setStatus(UserStatus.ACTIVE);
        user.setProfiles(Set.of(profileRepository.findByName(ProfileName.ADMIN).orElseThrow()));
        userRepository.save(user);

        return new AuthSeed(user.getEmail(), "SenhaForte123", company.getId());
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
        return today.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
    }

    private record AuthSeed(String email, String rawPassword, UUID companyId) {
    }

    private record CustomerSeed(UUID id) {
    }

    private record ServiceSeed(UUID id) {
    }

    private record EmployeeSeed(UUID id) {
    }

    private record ServiceOrderSeed(UUID id) {
    }
}
