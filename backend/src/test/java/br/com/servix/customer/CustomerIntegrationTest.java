package br.com.servix.customer;

import br.com.servix.auth.domain.Profile;
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
import br.com.servix.customer.dto.CustomerRequest;
import br.com.servix.customer.dto.CustomerStatusUpdateRequest;
import br.com.servix.customer.repository.CustomerRepository;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import br.com.servix.service.repository.ServiceRepository;
import br.com.servix.serviceorder.repository.ServiceOrderHistoryRepository;
import br.com.servix.serviceorder.repository.ServiceOrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceOrderHistoryRepository serviceOrderHistoryRepository;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private ProfileRepository profileRepository;

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
        serviceRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void shouldCreateSearchUpdateAndDeleteCustomerWithinTenant() throws Exception {
        AuthSeed companyA = seedAuthenticatedUser("a");
        String accessToken = login(companyA.email(), companyA.rawPassword());

        CustomerRequest request = customerRequest("Maria Silva", "123.456.789-00", "maria@servix.com", "11 99999-1111");
        MvcResult createResult = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.nome").value("Maria Silva"))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID customerId = UUID.fromString(createdJson.path("data").get("id").asText());

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("nome", "maria")
                        .param("ativo", "true")
                        .param("sortBy", "nome")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(customerId.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/customers/" + customerId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cpfCnpj").value("12345678900"));

        mockMvc.perform(put("/api/customers/" + customerId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest("Maria Silva Atualizada", "123.456.789-00", "maria@servix.com", "11 99999-1111"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Maria Silva Atualizada"));

        mockMvc.perform(patch("/api/customers/" + customerId + "/status")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomerStatusUpdateRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ativo").value(false));

        mockMvc.perform(delete("/api/customers/" + customerId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers/" + customerId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockAccessBetweenTenants() throws Exception {
        AuthSeed companyA = seedAuthenticatedUser("a");
        AuthSeed companyB = seedAuthenticatedUser("b");

        String tokenA = login(companyA.email(), companyA.rawPassword());
        String tokenB = login(companyB.email(), companyB.rawPassword());

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest("Cliente B", "987.654.321-00", "cliente.b@servix.com", "11 98888-2222"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID customerId = UUID.fromString(createdJson.path("data").get("id").asText());

        mockMvc.perform(get("/api/customers/" + customerId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRequireAuthenticationForCustomerEndpoints() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    private AuthSeed seedAuthenticatedUser(String suffix) {
        Company company = new Company();
        company.setNome("Empresa " + suffix);
        company.setDocumento(String.format("123456780001%02d", Math.abs(suffix.hashCode() % 90) + 10));
        company.setEmail("empresa." + suffix + "@servix.com");
        company.setStatus(CompanyStatus.ACTIVE);
        company = companyRepository.save(company);

        Profile profile = profileRepository.findByName(ProfileName.ADMIN).orElseThrow();

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

    private CustomerRequest customerRequest(String nome, String cpfCnpj, String email, String telefone) {
        return new CustomerRequest(
                nome,
                cpfCnpj,
                email,
                telefone,
                null,
                "12345678",
                "Rua A",
                "10",
                null,
                "Centro",
                "São Paulo",
                "SP",
                null);
    }

    private record AuthSeed(String email, String rawPassword, UUID companyId) {
    }
}
