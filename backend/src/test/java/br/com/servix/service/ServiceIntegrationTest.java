package br.com.servix.service;

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
import br.com.servix.service.dto.ServiceRequest;
import br.com.servix.service.dto.ServiceStatusUpdateRequest;
import br.com.servix.service.repository.ServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class ServiceIntegrationTest {

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
    private ServiceRepository serviceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void shouldCreateSearchUpdateAndDeleteServiceWithinTenant() throws Exception {
        AuthSeed seed = seedAuthenticatedUser("a");
        String token = login(seed.email(), seed.rawPassword());

        ServiceRequest request = new ServiceRequest("Limpeza", "Limpeza geral", 90, new BigDecimal("150.00"));
        MvcResult createResult = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Limpeza"))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID serviceId = UUID.fromString(createdJson.path("data").get("id").asText());

        mockMvc.perform(get("/api/services")
                        .header("Authorization", "Bearer " + token)
                        .param("name", "lim")
                        .param("active", "true")
                        .param("minPrice", "100")
                        .param("maxPrice", "200")
                        .param("minDuration", "60")
                        .param("maxDuration", "120")
                        .param("sortBy", "name")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(serviceId.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/services/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(150.0));

        mockMvc.perform(put("/api/services/" + serviceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceRequest("Limpeza Premium", "Atualizada", 120, new BigDecimal("220.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Limpeza Premium"));

        mockMvc.perform(patch("/api/services/" + serviceId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceStatusUpdateRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(delete("/api/services/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/services/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockCrossTenantAccess() throws Exception {
        AuthSeed seedA = seedAuthenticatedUser("a");
        AuthSeed seedB = seedAuthenticatedUser("b");

        String tokenA = login(seedA.email(), seedA.rawPassword());
        String tokenB = login(seedB.email(), seedB.rawPassword());

        MvcResult createResult = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServiceRequest("Manutenção", "Geral", 60, new BigDecimal("99.00")))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID serviceId = UUID.fromString(createdJson.path("data").get("id").asText());

        mockMvc.perform(get("/api/services/" + serviceId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRequireAuthenticationForServiceEndpoints() throws Exception {
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidServicePayload() throws Exception {
        AuthSeed seed = seedAuthenticatedUser("c");
        String token = login(seed.email(), seed.rawPassword());

        ServiceRequest invalid = new ServiceRequest("", "", 0, new BigDecimal("0.00"));

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
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

    private record AuthSeed(String email, String rawPassword, UUID companyId) {
    }
}
