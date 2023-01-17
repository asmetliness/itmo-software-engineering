package com.artefact.api.security;

import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.repository.*;
import com.artefact.api.request.CreateWeaponRequest;
import com.artefact.api.request.UpdateWeaponRequest;
import com.artefact.api.request.WeaponRequest;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.response.WeaponResponse;
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

import java.util.function.Consumer;

import static com.artefact.api.utils.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class WeaponModuleTests {

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
    void weapon_create_unauthorizedError() {
        var createWeapon = getCreateWeapon();

        var result = restTemplate.postForEntity(
                "/api/weapon",
                createWeapon,
                WeaponResponse.class
        );

        assertUnauthorized(result);
    }

    @Test
    void weapon_create_allFieldsValidationErrors() {
        createValidationHelper((weapon) -> {
            weapon.setTitle(null);
        });


        createValidationHelper((weapon) -> {
            weapon.setPrice(null);
        });
    }

    @Test
    void weapon_update_unauthorizedError() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = getUpdateWeapon(createResult.getBody().getWeapon().getId());

        var result = restTemplate.exchange("/api/weapon", HttpMethod.PUT, new HttpEntity<>(updateRequest), WeaponResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void weapon_update_allFieldsValidationErrors() {
        updateValidationHelper((weapon) -> {
            weapon.setId(null);
        });

        updateValidationHelper((weapon) -> {
            weapon.setTitle(null);
        });

        updateValidationHelper((weapon) -> {
            weapon.setPrice(null);
        });
    }


    @Test
    void weapon_delete_unauthorizedError() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var deleteResult = restTemplate.getForEntity(
                "/api/weapon/" + result.getBody().getWeapon().getId().toString(),
                ErrorResponse.class
        );

        assertUnauthorized(deleteResult);
    }

    @Test
    void weapon_getById_unauthorizedError() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var getResult = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/" + result.getBody().getWeapon().getId().toString(),
                null,
                ErrorResponse.class);

        assertUnauthorized(getResult);
    }

    @Test
    void weapon_getAvailable_unauthorizedError() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var result = restTemplate.getForEntity("/api/weapon/available", ErrorResponse.class);
        assertUnauthorized(result);
    }


    @Test
    void weapon_request_unauthorizedError() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var requestResult = restTemplate.postForEntity(
                "/api/weapon/request/" + result.getBody().getWeapon().getId(),
                null,
                WeaponResponse.class);

        assertUnauthorized(requestResult);
    }

    @Test
    void weapon_getRequested_unauthorizedError() {

        var result = restTemplate.getForEntity("/api/weapon/requested", ErrorResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void weapon_buy_unauthorizedError() {
        var weaponRequest = new WeaponRequest("spb");

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/buy/123",
                null,
                weaponRequest,
                ErrorResponse.class);
        assertUnauthorized(result);
    }

    @Test
    void weapon_confirm_unauthorizedError() {

        var result = restTemplate.postForEntity("/api/weapon/confirm/10",
                null,
                ErrorResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void weapon_decline_unauthorizedError() {
        var result = restTemplate.postForEntity("/api/weapon/decline/10",
                null,
                ErrorResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void weapon_suggest_unauthorizedError() {
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/suggest",
                null,
                ErrorResponse.class);
        assertUnauthorized(result);
    }


    @Test
    void weapon_courier_accept_unauthorizedError() {
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/courier/accept/123",
                null,
                ErrorResponse.class);
        assertUnauthorized(result);
    }

    @Test
    void weapon_courier_decline_unauthorizedError() {
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/courier/decline/123",
                null,
                ErrorResponse.class);
        assertUnauthorized(result);
    }

    @Test
    void weapon_courier_deliver_unauthorizedError() {
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/courier/deliver/123",
                null,
                ErrorResponse.class);
        assertUnauthorized(result);
    }



    private void createValidationHelper(Consumer<CreateWeaponRequest> func) {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();
        func.accept(createWeapon);
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                ErrorResponse.class);

        assertValidationError(result);
    }

    private void updateValidationHelper(Consumer<UpdateWeaponRequest> func) {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var createWeapon = getCreateWeapon();
        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = getUpdateWeapon(createResult.getBody().getWeapon().getId());
        func.accept(updateRequest);

        var result = putAuthorized(restTemplate,
                "/api/weapon",
                user,
                updateRequest,
                ErrorResponse.class);

        assertValidationError(result);
    }

}
