package com.artefact.api.utils;

import com.artefact.api.consts.Role;
import com.artefact.api.request.RegisterRequest;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.response.ErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

public class TestUtil {

    public static void assertNegativeResponse(ResponseEntity<ErrorResponse> response, HttpStatus errorCode, ErrorResponse errorMsg) {
        Assertions.assertEquals(errorCode, response.getStatusCode());
        Assertions.assertEquals(response.getBody(), errorMsg);
    }

    public static <T> void assertUnauthorized(ResponseEntity<T> response) {
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    public static void assertValidationError(ResponseEntity<ErrorResponse> response) {
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertTrue(response.getBody().getMessage().contains("Некорректно заполненные поля!"));
    }

    public static RegisterRequest createRegisterRequest() {
        return createRegisterRequest(Role.Client);
    }

        public static RegisterRequest createRegisterRequest(Role role) {

        var email = UUID.randomUUID().toString() + "@mail.ru";
        return  new RegisterRequest(
                email,
                "password",
                role
        );
    }


    public static AuthResponse register(TestRestTemplate restTemplate) {
        return registerRole(restTemplate, Role.Client);
    }

    public static AuthResponse registerRole(TestRestTemplate restTemplate, Role role) {
        var register = TestUtil.createRegisterRequest(role);

        var response = restTemplate
                .postForEntity("/api/auth/register", register, AuthResponse.class);

        return response.getBody();
    }

    public static <TRequest, TResponse> ResponseEntity<TResponse> postAuthorized(
            TestRestTemplate restTemplate,
            String url,
            AuthResponse auth,
            TRequest body,
            Class<TResponse> response) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization" , "Bearer " + auth.getToken());

        HttpEntity<TRequest> entity = new HttpEntity(body, headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, response);
    }

    public static <TRequest, TResponse> ResponseEntity<TResponse> putAuthorized(
            TestRestTemplate restTemplate,
            String url,
            AuthResponse auth,
            TRequest body,
            Class<TResponse> response) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization" , "Bearer " + auth.getToken());

        HttpEntity<TRequest> entity = new HttpEntity(body, headers);

        return restTemplate.exchange(url, HttpMethod.PUT, entity, response);
    }

    public static <TResponse> ResponseEntity<TResponse> getAuthorized(
            TestRestTemplate restTemplate,
            String url,
            AuthResponse auth,
            Class<TResponse> response) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization" , "Bearer " + auth.getToken());

        HttpEntity entity = new HttpEntity(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, response);
    }
}
