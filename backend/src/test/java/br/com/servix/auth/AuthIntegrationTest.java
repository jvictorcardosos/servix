package br.com.servix.auth;

import br.com.servix.auth.domain.ProfileName;
import br.com.servix.auth.dto.LoginRequest;
import br.com.servix.auth.dto.RegisterUserRequest;
import br.com.servix.auth.repository.RefreshTokenRepository;
import br.com.servix.auth.repository.UserRepository;
import br.com.servix.auth.service.JwtService;
import br.com.servix.company.domain.Company;
import br.com.servix.company.domain.CompanyStatus;
import br.com.servix.company.repository.CompanyRepository;
import br.com.servix.customer.repository.CustomerRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import br.com.servix.schedule.repository.AppointmentRepository;
import br.com.servix.schedule.repository.EmployeeRepository;
import br.com.servix.schedule.repository.WorkScheduleRepository;
import br.com.servix.service.repository.ServiceRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

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
    private JwtService jwtService;

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
    void shouldRegisterUser() throws Exception {
        Company company = createCompany();
        RegisterUserRequest request = new RegisterUserRequest(
                company.getId(),
                "Administrador",
                "admin@servix.com",
                "SenhaForte123",
                Set.of(ProfileName.ADMIN));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("admin@servix.com"))
                .andExpect(jsonPath("$.data.companyId").value(company.getId().toString()));
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        Company company = createCompany();
        registerUser(company.getId(), "user@servix.com", "SenhaForte123", Set.of(ProfileName.GESTOR));

        LoginRequest request = new LoginRequest("user@servix.com", "SenhaForte123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void shouldRejectInvalidLogin() throws Exception {
        Company company = createCompany();
        registerUser(company.getId(), "invalid@servix.com", "SenhaForte123", Set.of(ProfileName.OPERADOR));

        LoginRequest request = new LoginRequest("invalid@servix.com", "SenhaErrada123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGenerateTokenWithUserId() throws Exception {
        Company company = createCompany();
        registerUser(company.getId(), "token@servix.com", "SenhaForte123", Set.of(ProfileName.ADMIN));

        LoginRequest request = new LoginRequest("token@servix.com", "SenhaForte123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID userId = UUID.fromString(json.path("data").get("userId").asText());
        String accessToken = json.path("data").get("accessToken").asText();

        assertThat(jwtService.extractUserId(accessToken)).isEqualTo(userId);
    }

    @Test
    void shouldProtectEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldBlockCrossTenantRegistration() throws Exception {
        Company companyA = createCompany("a");
        Company companyB = createCompany("b");

        registerUser(companyA.getId(), "admin@a.com", "SenhaForte123", Set.of(ProfileName.ADMIN));
        String adminToken = loginAndGetAccessToken("admin@a.com", "SenhaForte123");

        RegisterUserRequest request = new RegisterUserRequest(
                companyB.getId(),
                "Usuário B",
                "user@b.com",
                "SenhaForte123",
                Set.of(ProfileName.OPERADOR));

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRefreshTokenAndInvalidateOldRefreshToken() throws Exception {
        Company company = createCompany("refresh");
        registerUser(company.getId(), "refresh@servix.com", "SenhaForte123", Set.of(ProfileName.ADMIN));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("refresh@servix.com", "SenhaForte123"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String oldRefresh = loginJson.path("data").get("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefresh + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode refreshJson = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String newRefresh = refreshJson.path("data").get("refreshToken").asText();
        assertThat(newRefresh).isNotEqualTo(oldRefresh);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefresh + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyLogoutForAnotherUserRefreshToken() throws Exception {
        Company company = createCompany("logout");
        registerUser(company.getId(), "admin.logout@servix.com", "SenhaForte123", Set.of(ProfileName.ADMIN));
        String adminToken = loginAndGetAccessToken("admin.logout@servix.com", "SenhaForte123");
        registerUserAs(
                adminToken,
                company.getId(),
                "operador.logout@servix.com",
                "SenhaForte123",
                Set.of(ProfileName.OPERADOR));

        LoginResult adminLogin = login("admin.logout@servix.com", "SenhaForte123");
        LoginResult operatorLogin = login("operador.logout@servix.com", "SenhaForte123");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + adminLogin.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + operatorLogin.refreshToken() + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnStandardErrorPayloadOnInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.details").isArray());
    }

    private Company createCompany() {
        return createCompany("base");
    }

    private Company createCompany(String suffix) {
        Company company = new Company();
        company.setNome("Servix " + suffix.toUpperCase());
        company.setDocumento("123456780001" + Math.abs(suffix.hashCode() % 90 + 10));
        company.setEmail("contato+" + suffix + "@servix.com");
        company.setStatus(CompanyStatus.ACTIVE);
        return companyRepository.save(company);
    }

    private void registerUser(UUID companyId, String email, String password, Set<ProfileName> profiles) throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                companyId,
                "Usuário Teste",
                email,
                password,
                profiles);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private void registerUserAs(String accessToken, UUID companyId, String email, String password, Set<ProfileName> profiles)
            throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                companyId,
                "Usuário Teste",
                email,
                password,
                profiles);

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        return login(email, password).accessToken();
    }

    private LoginResult login(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = json.path("data");
        return new LoginResult(data.get("accessToken").asText(), data.get("refreshToken").asText());
    }

    private record LoginResult(String accessToken, String refreshToken) {
    }
}
