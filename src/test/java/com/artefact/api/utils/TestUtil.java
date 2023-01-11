package com.artefact.api.utils;

import com.artefact.api.consts.Role;
import com.artefact.api.request.RegisterRequest;
import com.artefact.api.response.AuthResponse;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

public class TestUtil {

    public static void assertNegativeResponse(ResponseEntity<String> response, HttpStatus errorCode, String errorMsg) {
        Assertions.assertEquals(errorCode, response.getStatusCode());
        Assertions.assertEquals(response.getBody(), errorMsg);
    }

    public static RegisterRequest createRegisterRequest() {

        var email = UUID.randomUUID().toString() + "@mail.ru";
        return  new RegisterRequest(
                email,
                "password",
                Role.Client
        );
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
