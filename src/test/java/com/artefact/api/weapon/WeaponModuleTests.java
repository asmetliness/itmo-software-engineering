package com.artefact.api.weapon;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.repository.WeaponRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.CreateWeaponRequest;
import com.artefact.api.request.SuggestWeaponRequest;
import com.artefact.api.request.UpdateWeaponRequest;
import com.artefact.api.request.WeaponRequest;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.response.WeaponResponse;
import com.artefact.api.response.WeaponResponse;
import com.artefact.api.utils.ApiErrors;
import com.artefact.api.utils.TestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
import java.util.Arrays;
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
    static void cleanupData(@Autowired UserRepository userRepository, @Autowired WeaponRepository weaponRepository) {
        weaponRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    void weapon_create() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertEquals(createWeapon.getTitle(), result.getBody().getWeapon().getTitle());
        assertEquals(createWeapon.getDescription(), result.getBody().getWeapon().getDescription());
        assertEquals(createWeapon.getPrice(), result.getBody().getWeapon().getPrice());
        assertEquals(user.getUser().getId(), result.getBody().getCreatedUser().getId());
        Assertions.assertNull(result.getBody().getAcquiredUser());
        Assertions.assertNull(result.getBody().getRequestedUser());
        assertEquals(StatusIds.New, result.getBody().getStatus().getId());
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
    void weapon_create_wrongRoleError() {
        var user = TestUtil.registerRole(restTemplate, Role.Client);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertEquals(ApiErrors.Weapon.CantCreate, result.getBody());
    }



    @Test
    void weapon_create_allFieldsValidation() {
        createValidationHelper((weapon) -> {
            weapon.setTitle(null);
        });


        createValidationHelper((weapon) -> {
            weapon.setPrice(null);
        });
    }
    @Test
    void weapon_update() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = getUpdateWeapon(createResult.getBody().getWeapon().getId());

        var updateResult = TestUtil.putAuthorized(restTemplate,
                "/api/weapon",
                user,
                updateRequest,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        Assertions.assertNotNull(updateResult.getBody());
        assertEquals(updateRequest.getTitle(), updateResult.getBody().getWeapon().getTitle());
        assertEquals(updateRequest.getDescription(), updateResult.getBody().getWeapon().getDescription());
        assertEquals(updateRequest.getPrice(),updateResult.getBody().getWeapon().getPrice());
        assertEquals(user.getUser().getId(), updateResult.getBody().getCreatedUser().getId());
        Assertions.assertNull(updateResult.getBody().getAcquiredUser());
        Assertions.assertNull(updateResult.getBody().getRequestedUser());
        assertEquals(StatusIds.New, updateResult.getBody().getStatus().getId());

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
    void weapon_update_cantUpdateOthersWeapon() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var otherUser = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = getUpdateWeapon(createResult.getBody().getWeapon().getId());

        var result = putAuthorized(restTemplate,
                "/api/weapon",
                otherUser,
                updateRequest,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertEquals(ApiErrors.Weapon.CantEdit, result.getBody());
    }

    @Test
    void weapon_update_updateNotFound() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var updateRequest = getUpdateWeapon(123123L);

        var result = putAuthorized(restTemplate,
                "/api/weapon",
                user,
                updateRequest,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        assertEquals(ApiErrors.Weapon.NotFound, result.getBody());
    }


    @Test
    void weapon_update_allFieldsValidation() {
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
    void weapon_delete() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var getBeforeDelete = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/" + result.getBody().getWeapon().getId().toString(),
                user,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, getBeforeDelete.getStatusCode());
        assertEquals(result.getBody().getWeapon().getId(), getBeforeDelete.getBody().getWeapon().getId());

        var deleteResult = TestUtil.deleteAuthorized(restTemplate,
                "/api/weapon/" + result.getBody().getWeapon().getId().toString(),
                user,
                WeaponResponse[].class);

        assertEquals(HttpStatus.OK, deleteResult.getStatusCode());

        var getAfterDelete = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/" + result.getBody().getWeapon().getId().toString(),
                user,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, getAfterDelete.getStatusCode());
        assertEquals(ApiErrors.Weapon.NotFound, getAfterDelete.getBody());

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
    void weapon_delete_accessViolationError() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var user2 = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var deleteResult = TestUtil.deleteAuthorized(restTemplate,
                "/api/weapon/" + result.getBody().getWeapon().getId().toString(),
                user2,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, deleteResult.getStatusCode());
        assertEquals(ApiErrors.Weapon.CantDelete, deleteResult.getBody());
    }

    @Test
    void weapon_getById() {
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
                user,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertEquals(result.getBody().getWeapon().getId(), getResult.getBody().getWeapon().getId());
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
    void weapon_getById_notFoundError() {
        var user = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                user,
                createWeapon,
                WeaponResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var getResult = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/" + 13123123,
                user,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, getResult.getStatusCode());
        assertEquals(ApiErrors.Weapon.NotFound, getResult.getBody());
    }



    @Test
    void weapon_getAvailable() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var stalkerResult = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/available",
                stalker,
                WeaponResponse[].class);

        assertEquals(HttpStatus.OK, stalkerResult.getStatusCode());
        Assertions.assertTrue(stalkerResult.getBody().length > 0);
        Assertions.assertTrue(Arrays.stream(stalkerResult.getBody())
                .anyMatch(weapon -> weapon.getWeapon().getId().equals(result.getBody().getWeapon().getId())));

        var clientResult = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/available",
                client,
                WeaponResponse[].class);
        assertEquals(HttpStatus.OK, clientResult.getStatusCode());
        assertEquals(0, clientResult.getBody().length);
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
    void weapon_request_fullProcess() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var weaponDealer2 = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var weaponRequest = new WeaponRequest("spb");
        var requestResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/request/" + result.getBody().getWeapon().getId(),
                stalker,
                weaponRequest,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, requestResult.getStatusCode());
        assertEquals(StatusIds.Requested, requestResult.getBody().getWeapon().getStatusId());
        Assertions.assertNotNull(requestResult.getBody().getRequestedUser());
        assertEquals(stalker.getUser().getId(), requestResult.getBody().getRequestedUser().getId());
        Assertions.assertNull(requestResult.getBody().getAcquiredUser());

        var getRequestedResult = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/requested",
                weaponDealer,
                WeaponResponse[].class);
        assertEquals(HttpStatus.OK, getRequestedResult.getStatusCode());
        Assertions.assertTrue(getRequestedResult.getBody().length > 0);
        Assertions.assertTrue(Arrays.stream(getRequestedResult.getBody()).anyMatch(i ->
                i.getWeapon().getId().equals(result.getBody().getWeapon().getId())));

        var getStalkerRequestedResult = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/requested",
                stalker,
                WeaponResponse[].class);
        assertEquals(HttpStatus.OK, getStalkerRequestedResult.getStatusCode());
        Assertions.assertTrue(getStalkerRequestedResult.getBody().length > 0);
        Assertions.assertTrue(Arrays.stream(getStalkerRequestedResult.getBody()).anyMatch(i ->
                i.getWeapon().getId().equals(result.getBody().getWeapon().getId())));

        var getEmptyRequestedResult = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/requested",
                weaponDealer2,
                WeaponResponse[].class);
        assertEquals(HttpStatus.OK, getEmptyRequestedResult.getStatusCode());
        assertEquals(0, getEmptyRequestedResult.getBody().length);

        var getAvailableResult = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/available",
                stalker,
                WeaponResponse[].class);

        assertEquals(HttpStatus.OK, getAvailableResult.getStatusCode());
        Assertions.assertFalse(Arrays.stream(getAvailableResult.getBody()).anyMatch(i ->
                i.getWeapon().getId().equals(result.getBody().getWeapon().getId())));
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
    void weapon_confirm_fullProcess_fullErrors() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var weaponDealer2 = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var weaponRequest = new WeaponRequest("spb");
        var requestResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/request/" + result.getBody().getWeapon().getId(),
                stalker,
                weaponRequest,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, requestResult.getStatusCode());
        assertEquals(StatusIds.Requested, requestResult.getBody().getWeapon().getStatusId());

        var errorConfirmResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/confirm/" + result.getBody().getWeapon().getId(),
                weaponDealer2,
                ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorConfirmResult.getStatusCode());
        assertEquals(ApiErrors.Weapon.CantConfirm, errorConfirmResult.getBody());

        var notFoundResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/confirm/" +123123123,
                weaponDealer,
                ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
        assertEquals(ApiErrors.Weapon.NotFound, notFoundResult.getBody());

        var confirmResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/confirm/" + result.getBody().getWeapon().getId(),
                weaponDealer,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, confirmResult.getStatusCode());
        assertEquals(StatusIds.Acquired, confirmResult.getBody().getWeapon().getStatusId());
        assertEquals(stalker.getUser().getId(), confirmResult.getBody().getAcquiredUser().getId());
        Assertions.assertNull(confirmResult.getBody().getRequestedUser());

        var stalkerGetAcquired = TestUtil.getAuthorized(restTemplate,
                "/api/weapon",
                stalker,
                WeaponResponse[].class);
        assertEquals(HttpStatus.OK, stalkerGetAcquired.getStatusCode());
        Assertions.assertTrue(stalkerGetAcquired.getBody().length > 0);

        Assertions.assertTrue(Arrays.stream(stalkerGetAcquired.getBody()).anyMatch(i ->
                i.getWeapon().getId().equals(result.getBody().getWeapon().getId())));
    }

    @Test
    void weapon_confirm_unauthorizedError() {

        var result = restTemplate.postForEntity("/api/weapon/confirm/10",
                null,
                ErrorResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void weapon_decline_fullProcess_fullErrors() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var weaponDealer2 = TestUtil.registerRole(restTemplate, Role.WeaponDealer);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var weaponRequest = new WeaponRequest("spb");
        var requestResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/request/" + result.getBody().getWeapon().getId(),
                stalker,
                weaponRequest,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, requestResult.getStatusCode());
        assertEquals(StatusIds.Requested, requestResult.getBody().getWeapon().getStatusId());

        var errorConfirmResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/decline/" + result.getBody().getWeapon().getId(),
                weaponDealer2,
                ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorConfirmResult.getStatusCode());
        assertEquals(ApiErrors.Weapon.CantDecline, errorConfirmResult.getBody());

        var notFoundResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/decline/" +123123123,
                weaponDealer,
                ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
        assertEquals(ApiErrors.Weapon.NotFound, notFoundResult.getBody());

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/decline/" + result.getBody().getWeapon().getId(),
                weaponDealer,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, declineResult.getStatusCode());
        assertEquals(StatusIds.New, declineResult.getBody().getWeapon().getStatusId());
        Assertions.assertNull(declineResult.getBody().getRequestedUser());
        Assertions.assertNull(declineResult.getBody().getAcquiredUser());

        var stalkerGetAvailable = TestUtil.getAuthorized(restTemplate,
                "/api/weapon/available",
                stalker,
                WeaponResponse[].class);
        assertEquals(HttpStatus.OK, stalkerGetAvailable.getStatusCode());
        Assertions.assertTrue(stalkerGetAvailable.getBody().length > 0);

        Assertions.assertTrue(Arrays.stream(stalkerGetAvailable.getBody()).anyMatch(i ->
                i.getWeapon().getId().equals(result.getBody().getWeapon().getId())));

        var stalkerGetAcquired = TestUtil.getAuthorized(restTemplate,
                "/api/weapon",
                stalker,
                WeaponResponse[].class);

        assertEquals(HttpStatus.OK, stalkerGetAcquired.getStatusCode());
        assertEquals(0, stalkerGetAcquired.getBody().length);
    }

    @Test
    void weapon_decline_unauthorizedError() {
        var result = restTemplate.postForEntity("/api/weapon/decline/10",
                null,
                ErrorResponse.class);

        assertUnauthorized(result);
    }


    @Test
    void weapon_suggest() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var weaponRequest = new WeaponRequest("spb");
        var requestResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/request/" + result.getBody().getWeapon().getId(),
                stalker,
                weaponRequest,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, requestResult.getStatusCode());

        var confirmResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/confirm/" + result.getBody().getWeapon().getId(),
                weaponDealer,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, confirmResult.getStatusCode());

        var suggestRequest = getSuggestRequest(courier, result.getBody());

        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/suggest",
                weaponDealer,
                suggestRequest,
                WeaponResponse.class);

        assertOK(suggestResult);
        assertEquals(courier.getUser().getId(), suggestResult.getBody().getSuggestedCourier().getId());
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
    void weapon_suggest_notFoundError() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var suggestRequest = getSuggestRequest(courier, result.getBody());
        suggestRequest.setWeaponId(123123L);
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/suggest",
                weaponDealer,
                suggestRequest,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, suggestResult.getStatusCode());
        assertEquals(ApiErrors.Weapon.NotFound, suggestResult.getBody());
    }

    @Test
    void weapon_suggest_wrongRole() {
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);

        var createWeapon = getCreateWeapon();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/weapon",
                weaponDealer,
                createWeapon,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var weaponRequest = new WeaponRequest("spb");
        var requestResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/request/" + result.getBody().getWeapon().getId(),
                stalker,
                weaponRequest,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, requestResult.getStatusCode());

        var confirmResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/confirm/" + result.getBody().getWeapon().getId(),
                weaponDealer,
                WeaponResponse.class);
        assertEquals(HttpStatus.OK, confirmResult.getStatusCode());

        var suggestRequest = getSuggestRequest(stalker, result.getBody());

        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/weapon/suggest",
                weaponDealer,
                suggestRequest,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, suggestResult.getStatusCode());
        assertEquals(ApiErrors.Weapon.CantSuggestToUser, suggestResult.getBody());
    }


    private SuggestWeaponRequest getSuggestRequest(AuthResponse courier, WeaponResponse weaponResponse) {

        return new SuggestWeaponRequest(weaponResponse.getWeapon().getId(), courier.getUser().getId());
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

    private CreateWeaponRequest getCreateWeapon() {
        return new CreateWeaponRequest(
                "title",
                "description",
                new BigDecimal(100));
    }


    private UpdateWeaponRequest getUpdateWeapon(Long id) {
        return new UpdateWeaponRequest(id,
                "newTitle",
                "newDescription",
                new BigDecimal(50));
    }

}
