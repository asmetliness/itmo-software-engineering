package com.artefact.api.security;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.repository.*;
import com.artefact.api.request.CreateInformationRequest;
import com.artefact.api.request.UpdateInformationRequest;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.response.InformationResponse;
import com.artefact.api.utils.TestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static com.artefact.api.utils.TestUtil.getCreateInformation;
import static com.artefact.api.utils.TestUtil.getUpdateInformation;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class InformationModuleTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterAll
    static void cleanupData(
            @Autowired NotificationRepository notificationRepository,
            @Autowired UserRepository userRepository,
            @Autowired InformationRepository informationRepository,
            @Autowired WeaponRepository weaponRepository,
            @Autowired OrderRepository orderRepository) {
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
        weaponRepository.deleteAll();
        informationRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Test
    void information_create_unauthorizedError() {
        var createInformation = getCreateInformation();

        var result = restTemplate.postForEntity(
                "/api/information",
                createInformation,
                InformationResponse.class
        );

        TestUtil.assertUnauthorized(result);
    }

    @Test
    void information_update_unauthorizedError() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = new UpdateInformationRequest(createResult.getBody().getInformation().getId(),
                "newTitle",
                "newDescription",
                "newInformation",
                new BigDecimal(50));

        var result = restTemplate.exchange("/api/information", HttpMethod.PUT, new HttpEntity<>(updateRequest), InformationResponse.class);

        TestUtil.assertUnauthorized(result);
    }



    @Test
    void information_create_allFieldsValidationErrors() {
        createValidationHelper((info) -> {
            info.setTitle(null);
        });

        createValidationHelper((info) -> {
            info.setDescription(null);
        });

        createValidationHelper((info) -> {
            info.setInformation(null);
        });

        createValidationHelper((info) -> {
            info.setPrice(null);
        });
    }


    @Test
    void information_update_allFieldsValidationErrors() {
        updateValidationHelper((info) -> {
            info.setId(null);
        });

        updateValidationHelper((info) -> {
            info.setTitle(null);
        });

        updateValidationHelper((info) -> {
            info.setDescription(null);
        });

        updateValidationHelper((info) -> {
            info.setInformation(null);
        });

        updateValidationHelper((info) -> {
            info.setPrice(null);
        });
    }


    @Test
    void information_getById_unauthorizedError() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var getResult = TestUtil.getAuthorized(restTemplate,
                "/api/information/" + result.getBody().getInformation().getId().toString(),
                null,
                ErrorResponse.class);

        TestUtil.assertUnauthorized(getResult);
    }

    @Test
    void information_delete_unauthorizedError() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var deleteResult = restTemplate.getForEntity(
                "/api/information/" + result.getBody().getInformation().getId().toString(),
                ErrorResponse.class
        );

        TestUtil.assertUnauthorized(deleteResult);
    }

    @Test
    void information_getAvailable_unauthorizedError() {
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                informer,
                createInformation,
                InformationResponse.class);
        assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var result = restTemplate.getForEntity("/api/information/available", ErrorResponse.class);
        TestUtil.assertUnauthorized(result);
    }

    @Test
    void information_buy_unauthorizedError() {
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information/buy/123",
                null,
                ErrorResponse.class);
        TestUtil.assertUnauthorized(result);
    }

    @Test
    void information_request_unauthorizedError() {
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                informer,
                createInformation,
                InformationResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var requestResult = restTemplate.postForEntity(
                "/api/information/request/" + result.getBody().getInformation().getId(),
                null,
                InformationResponse.class);

        TestUtil.assertUnauthorized(requestResult);
    }

    @Test
    void information_getRequested_unauthorizedError() {

        var result = restTemplate.getForEntity("/api/information/requested", ErrorResponse.class);

        TestUtil.assertUnauthorized(result);
    }

    @Test
    void information_confirm_unauthorizedError() {

        var result = restTemplate.postForEntity("/api/information/confirm/10",
                null,
                ErrorResponse.class);

        TestUtil.assertUnauthorized(result);
    }

    @Test
    void information_decline_unauthorizedError() {
        var result = restTemplate.postForEntity("/api/information/decline/10",
                null,
                ErrorResponse.class);

        TestUtil.assertUnauthorized(result);
    }


    private void updateValidationHelper(Consumer<UpdateInformationRequest> func) {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);
        var createInformation = getCreateInformation();
        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = getUpdateInformation(createResult.getBody().getInformation().getId());
        func.accept(updateRequest);

        var result = TestUtil.putAuthorized(restTemplate,
                "/api/information",
                user,
                updateRequest,
                ErrorResponse.class);

        TestUtil.assertValidationError(result);
    }

    private void createValidationHelper(Consumer<CreateInformationRequest> func) {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();
        func.accept(createInformation);
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                ErrorResponse.class);

        TestUtil.assertValidationError(result);
    }
}
