package com.artefact.api.functional;

import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.LoginRequest;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.utils.ApiErrors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

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
    void register_getRoles() {
        var result = restTemplate.getForEntity("/api/roles", Role[].class);

        assertOK(result);
        Assertions.assertEquals(6, result.getBody().length);
    }

    @Test
    public void register_success() {
        var request = createRegisterRequest();

        var response = this.restTemplate
                .postForEntity("/api/auth/register", request, AuthResponse.class);

        assertPositiveResponse(response, request.getEmail());

        Assertions.assertEquals(request.getEmail(), response.getBody().getUser().getEmail());
        Assertions.assertEquals(request.getRole(), response.getBody().getUser().getRole());
    }


    @Test
    public void register_error_emailAlreadyUsed() {
        var request = createRegisterRequest();

        var response = this.restTemplate
                .postForEntity("/api/auth/register", request, AuthResponse.class);

        assertPositiveResponse(response, request.getEmail());

        var errorResponse = this.restTemplate
                .postForEntity("/api/auth/register", request, ErrorResponse.class);

        assertNegativeResponse(errorResponse, HttpStatus.BAD_REQUEST, ApiErrors.Auth.UserAlreadyExists);
    }

    @Test
    public void login_success() {
        var request = createRegisterRequest();

        var response = this.restTemplate
                .postForEntity("/api/auth/register", request, AuthResponse.class);

        assertPositiveResponse(response, request.getEmail());

        var loginRequest = new LoginRequest(request.getEmail(), request.getPassword());

        var loginResponse = this.restTemplate
                .postForEntity("/api/auth/login", loginRequest, AuthResponse.class);

        assertPositiveResponse(loginResponse, request.getEmail());
    }

    @Test
    public void login_error_userDoesNotExists() {
        var request = createRegisterRequest();
        var loginRequest = new LoginRequest(request.getEmail(), request.getPassword());
        var loginResponse = this.restTemplate
                .postForEntity("/api/auth/login", loginRequest, ErrorResponse.class);

        assertNegativeResponse(loginResponse, HttpStatus.NOT_FOUND, ApiErrors.Auth.UserNotFound);
    }

    @Test
    public void login_error_wrongPassword() {
        var request = createRegisterRequest();

        var response = this.restTemplate
                .postForEntity("/api/auth/register", request, AuthResponse.class);

        assertPositiveResponse(response, request.getEmail());

        var loginRequest = new LoginRequest(request.getEmail(), "wrong password");

        var loginResponse = this.restTemplate
                .postForEntity("/api/auth/login", loginRequest, ErrorResponse.class);

        assertNegativeResponse(loginResponse, HttpStatus.NOT_FOUND, ApiErrors.Auth.UserNotFound);
    }




    private void assertPositiveResponse(ResponseEntity<AuthResponse> response, String email) {
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getToken());
        Assertions.assertEquals(response.getBody().getUser().getEmail(), email);
    }



}
