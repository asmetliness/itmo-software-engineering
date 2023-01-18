package com.artefact.api.utils;

import com.artefact.api.consts.Role;
import com.artefact.api.request.*;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.response.ErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().getMessage().contains("Некорректно заполненные поля!"));
    }

    public static RegisterRequest createRegisterRequest() {
        return createRegisterRequest(Role.Client);
    }

    public static LoginRequest createLoginRequest() {
        var email = UUID.randomUUID().toString() + "@mail.ru";
        return  new LoginRequest(
                email,
                "password"
        );
    }

    public static CreateInformationRequest getCreateInformation() {
        return new CreateInformationRequest(
                "title",
                "description",
                "information",
                new BigDecimal(100));
    }

    public static UpdateInformationRequest getUpdateInformation(Long id) {
        return new UpdateInformationRequest(id,
                "newTitle",
                "newDescription",
                "newInformation",
                new BigDecimal(50));
    }

        public static RegisterRequest createRegisterRequest(Role role) {

        var email = UUID.randomUUID().toString() + "@mail.ru";
        return  new RegisterRequest(
                email,
                "password",
                role
        );
    }

    public static CreateWeaponRequest getCreateWeapon() {
        return new CreateWeaponRequest(
                "title",
                "description",
                new BigDecimal(100));
    }

    public static UpdateWeaponRequest getUpdateWeapon(Long id) {
        return new UpdateWeaponRequest(id,
                "newTitle",
                "newDescription",
                new BigDecimal(50));
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
        if(auth != null) {
            headers.add("Authorization" , "Bearer " + auth.getToken());
        }

        HttpEntity<TRequest> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, response);
    }

    public static <TRequest, TResponse> ResponseEntity<TResponse> postAuthorized(
            TestRestTemplate restTemplate,
            String url,
            AuthResponse auth,
            Class<TResponse> response) {

        HttpHeaders headers = new HttpHeaders();
        if(auth != null) {
            headers.add("Authorization" , "Bearer " + auth.getToken());
        }

        HttpEntity<Object> entity = new HttpEntity<Object>(headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, response);
    }

    public static <TRequest, TResponse> ResponseEntity<TResponse> postFile(
            TestRestTemplate restTemplate,
            String url,
            AuthResponse auth,
            String fileName,
            Class<TResponse> response) throws IOException {

        var file =ResourceUtils.getFile("classpath:" + fileName);
        var bytes = new FileInputStream(file).readAllBytes();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if(auth != null) {
            headers.add("Authorization" , "Bearer " + auth.getToken());
        }

        HttpHeaders parts = new HttpHeaders();
        parts.setContentType(MediaType.TEXT_PLAIN);
        final ByteArrayResource byteArrayResource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        final HttpEntity<ByteArrayResource> partsEntity = new HttpEntity<>(byteArrayResource, parts);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("image", partsEntity);

         return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(requestMap, headers), response);

    }
    public static <TRequest, TResponse> ResponseEntity<TResponse> putAuthorized(
            TestRestTemplate restTemplate,
            String url,
            AuthResponse auth,
            TRequest body,
            Class<TResponse> response) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization" , "Bearer " + auth.getToken());

        HttpEntity<TRequest> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.PUT, entity, response);
    }

    public static <TResponse> ResponseEntity<TResponse> getAuthorized(
            TestRestTemplate restTemplate,
            String url,
            AuthResponse auth,
            Class<TResponse> response) {

        HttpHeaders headers = new HttpHeaders();
        if(auth != null) {
            headers.add("Authorization" , "Bearer " + auth.getToken());
        }

        HttpEntity<Object> entity = new HttpEntity<Object>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, response);
    }

    public static <TResponse> ResponseEntity<TResponse> deleteAuthorized(
            TestRestTemplate restTemplate,
            String url,
            AuthResponse auth,
            Class<TResponse> response) {

        HttpHeaders headers = new HttpHeaders();
        if(auth != null)   headers.add("Authorization" , "Bearer " + auth.getToken());

        HttpEntity<Object> entity = new HttpEntity<Object>(headers);

        return restTemplate.exchange(url, HttpMethod.DELETE, entity, response);
    }



    public static <T> void assertOK(ResponseEntity<T> response) {
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }
}
