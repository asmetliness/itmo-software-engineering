package com.artefact.api.security;

import com.artefact.api.ApiApplication;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.response.ErrorResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static com.artefact.api.utils.TestUtil.*;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class AuthModuleTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterAll
    static void cleanupData(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @Test
    public void register_validation_emptyEmail() {
        var request = createRegisterRequest();
        request.setEmail(null);
        var response = this.restTemplate
                .postForEntity("/api/auth/register", request, ErrorResponse.class);
        assertValidationError(response);
    }

    @Test
    public void register_validation_emptyPassword() {
        var request = createRegisterRequest();
        request.setPassword(null);
        var response = this.restTemplate
                .postForEntity("/api/auth/register", request, ErrorResponse.class);
        assertValidationError(response);
    }

    @Test
    public void register_validation_emptyRole() {
        var request = createRegisterRequest();
        request.setRole(null);
        var response = this.restTemplate
                .postForEntity("/api/auth/register", request, ErrorResponse.class);
        assertValidationError(response);
    }

    @Test
    public void login_validation_emptyEmail() {
        var request = createLoginRequest();
        request.setEmail(null);
        var response = this.restTemplate
                .postForEntity("/api/auth/login", request, ErrorResponse.class);
        assertValidationError(response);
    }

    @Test
    public void login_validation_emptyPassword() {
        var request = createLoginRequest();
        request.setPassword(null);
        var response = this.restTemplate
                .postForEntity("/api/auth/login", request, ErrorResponse.class);
        assertValidationError(response);
    }
}
