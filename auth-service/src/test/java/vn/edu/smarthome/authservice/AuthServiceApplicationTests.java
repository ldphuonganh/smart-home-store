package vn.edu.smarthome.authservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.smarthome.authservice.entity.Role;
import vn.edu.smarthome.authservice.entity.User;
import vn.edu.smarthome.authservice.repository.ApiKeyRepository;
import vn.edu.smarthome.authservice.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Test tích hợp auth-service (H2, không cần MySQL). Chuột phải -> Run. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthServiceApplicationTests {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ApiKeyRepository apiKeyRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        apiKeyRepository.deleteAll();
        userRepository.deleteAll();
        User admin = new User();
        admin.setFullName("Admin");
        admin.setEmail("admin@smarthome.vn");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    @Test
    void registerLoginAndGetMe() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Nguyen Van A", "email", "A@Gmail.com",
                                "password", "123456", "phone", "0912345678"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("a@gmail.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        String token = login("a@gmail.com", "123456");

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.phone").value("0912345678"));

        mvc.perform(put("/auth/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Nguyen Van B", "phone", "0987654321"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyen Van B"));
    }

    @Test
    void loginResponseMatchesContract() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "admin@smarthome.vn", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void registerCannotChooseAdminRoleAndDuplicateEmailIs409() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Hacker", "email", "h@gmail.com",
                                "password", "123456", "role", "ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "X", "email", "ADMIN@smarthome.vn", "password", "123456"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/auth/register"));
    }

    @Test
    void invalidRegisterReturns400WithFieldErrors() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "", "email", "khong-phai-email", "password", "1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "admin@smarthome.vn", "password", "sai"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email hoặc mật khẩu không chính xác"));
    }

    @Test
    void meWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void adminCanLockUserAndLockedUserCannotLogin() throws Exception {
        String adminToken = login("admin@smarthome.vn", "admin123");
        String body = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "C", "email", "c@gmail.com", "password", "123456"))))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long userId = objectMapper.readTree(body).get("id").asLong();
        String customerToken = login("c@gmail.com", "123456");

        mvc.perform(get("/admin/users").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(put("/admin/users/{id}/status", userId).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "c@gmail.com", "password", "123456"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void apiKeyLifecycleAndInternalValidation() throws Exception {
        String adminToken = login("admin@smarthome.vn", "admin123");

        String body = mvc.perform(post("/api-keys").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("ownerName", "Partner A", "scopes", "products:read", "validDays", 30))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keyValue", startsWith("shp_")))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode key = objectMapper.readTree(body);

        mvc.perform(get("/internal/api-keys/validate")
                        .param("key", key.get("keyValue").asText()).param("scope", "products:read"))
                .andExpect(jsonPath("$.valid").value(true));
        mvc.perform(get("/internal/api-keys/validate")
                        .param("key", key.get("keyValue").asText()).param("scope", "orders:read"))
                .andExpect(jsonPath("$.valid").value(false));

        mvc.perform(delete("/api-keys/{id}", key.get("id").asLong()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        mvc.perform(get("/internal/api-keys/validate")
                        .param("key", key.get("keyValue").asText()).param("scope", "products:read"))
                .andExpect(jsonPath("$.valid").value(false));

        mvc.perform(post("/api-keys").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("ownerName", "B", "scopes", "sai scope"))))
                .andExpect(status().isBadRequest());
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
