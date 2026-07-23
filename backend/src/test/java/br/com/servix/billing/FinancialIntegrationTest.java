package br.com.servix.billing;

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
import br.com.servix.schedule.domain.Employee;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import br.com.servix.schedule.dto.EmployeeRequest;
import br.com.servix.schedule.dto.EmployeeScheduleRequest;
import br.com.servix.service.domain.ServiceOffering;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.serviceorder.domain.ServiceOrderStatus;
import br.com.servix.serviceorder.domain.ServiceOrder;
import br.com.servix.serviceorder.repository.ServiceOrderHistoryRepository;
import br.com.servix.serviceorder.repository.ServiceOrderRepository;
import br.com.servix.serviceorder.dto.ServiceOrderRequest;
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
class FinancialIntegrationTest {

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
    private br.com.servix.billing.repository.FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
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
    void shouldCreatePayAndAutoGenerateFinancialTransactionsWithinTenant() throws Exception {
        AuthSeed seed = seedAuthenticatedUser("a");
        String token = login(seed.email(), seed.rawPassword());

        CustomerSeed customer = createCustomer(token, "a");
        ServiceSeed service = createService(token);
        EmployeeSeed employee = createEmployee(token, "a");

        MvcResult transactionResult = mockMvc.perform(post("/api/financial")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new br.com.servix.billing.dto.FinancialTransactionRequest(
                                null,
                                new BigDecimal("200.00"),
                                new BigDecimal("10.00"),
                                new BigDecimal("5.00"),
                                LocalDate.now().plusDays(5),
                                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"),
                                "Lançamento manual",
                                "REF-100"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        JsonNode transactionJson = objectMapper.readTree(transactionResult.getResponse().getContentAsString());
        UUID transactionId = UUID.fromString(transactionJson.path("data").get("id").asText());

        mockMvc.perform(patch("/api/financial/" + transactionId + "/pay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new br.com.servix.billing.dto.FinancialPaymentRequest(
                                new BigDecimal("50.00"),
                                java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"),
                                LocalDate.now(),
                                "PAY-1",
                                "Parcial"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_PAID"));

        mockMvc.perform(get("/api/financial/" + transactionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingAmount").value(145.0));

        MvcResult serviceOrderResult = mockMvc.perform(post("/api/service-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceOrderRequest(
                                null,
                                customer.id(),
                                employee.id(),
                                service.id(),
                                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                                "OS financeira"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode serviceOrderJson = objectMapper.readTree(serviceOrderResult.getResponse().getContentAsString());
        UUID serviceOrderId = UUID.fromString(serviceOrderJson.path("data").get("id").asText());

        mockMvc.perform(patch("/api/service-orders/" + serviceOrderId + "/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/service-orders/" + serviceOrderId + "/finish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(get("/api/financial/service-order/" + serviceOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].serviceOrderId").value(serviceOrderId.toString()));

        mockMvc.perform(delete("/api/financial/" + transactionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBlockCrossTenantAccess() throws Exception {
        AuthSeed seedA = seedAuthenticatedUser("a");
        AuthSeed seedB = seedAuthenticatedUser("b");

        String tokenA = login(seedA.email(), seedA.rawPassword());
        String tokenB = login(seedB.email(), seedB.rawPassword());

        MvcResult result = mockMvc.perform(post("/api/financial")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new br.com.servix.billing.dto.FinancialTransactionRequest(
                                null,
                                new BigDecimal("100.00"),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                LocalDate.now().plusDays(3),
                                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"),
                                "Tenant B",
                                "B-REF"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID transactionId = UUID.fromString(json.path("data").get("id").asText());

        mockMvc.perform(get("/api/financial/" + transactionId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRequireAuthenticationForFinancialEndpoints() throws Exception {
        mockMvc.perform(get("/api/financial"))
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

    private record AuthSeed(String email, String rawPassword, UUID companyId) {
    }

    private record CustomerSeed(UUID id) {
    }

    private record ServiceSeed(UUID id) {
    }

    private record EmployeeSeed(UUID id) {
    }
}
