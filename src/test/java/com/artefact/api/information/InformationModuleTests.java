package com.artefact.api.information;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Information;
import com.artefact.api.repository.InformationRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.CreateInformationRequest;
import com.artefact.api.request.UpdateInformationRequest;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.response.InformationResponse;
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
    static void cleanupData(@Autowired UserRepository userRepository, @Autowired InformationRepository informationRepository) {
        informationRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    void information_create() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals("title", result.getBody().getInformation().getTitle());
        Assertions.assertEquals("description", result.getBody().getInformation().getDescription());
        Assertions.assertEquals("information",result.getBody().getInformation().getInformation());
        Assertions.assertEquals(new BigDecimal(100), result.getBody().getInformation().getPrice());
        Assertions.assertEquals(user.getUser().getId(), result.getBody().getCreatedUser().getId());
        Assertions.assertNull(result.getBody().getAcquiredUser());
        Assertions.assertNull(result.getBody().getRequestedUser());
        Assertions.assertEquals(StatusIds.New, result.getBody().getStatus().getId());
    }

    @Test
    void information_create_unauthorizedError() {
        var createInformation = getCreateInformation();

        var result = restTemplate.postForEntity(
                "/api/information",
                createInformation,
                InformationResponse.class
        );

        assertUnauthorized(result);
    }

    @Test
    void information_create_wrongRoleError() {
        var user = TestUtil.registerRole(restTemplate, Role.Client);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        Assertions.assertEquals(ApiErrors.Information.CreateError, result.getBody());
    }



    @Test
    void information_create_allFieldsValidation() {
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
    void information_update() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = new UpdateInformationRequest(createResult.getBody().getInformation().getId(),
                "newTitle",
                "newDescription",
                "newInformation",
                new BigDecimal(50));

        var updateResult = TestUtil.putAuthorized(restTemplate,
                "/api/information",
                user,
                updateRequest,
                InformationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        Assertions.assertNotNull(updateResult.getBody());
        Assertions.assertEquals("newTitle", updateResult.getBody().getInformation().getTitle());
        Assertions.assertEquals("newDescription", updateResult.getBody().getInformation().getDescription());
        Assertions.assertEquals("newInformation",updateResult.getBody().getInformation().getInformation());
        Assertions.assertEquals(new BigDecimal(50), updateResult.getBody().getInformation().getPrice());
        Assertions.assertEquals(user.getUser().getId(), updateResult.getBody().getCreatedUser().getId());
        Assertions.assertNull(updateResult.getBody().getAcquiredUser());
        Assertions.assertNull(updateResult.getBody().getRequestedUser());
        Assertions.assertEquals(StatusIds.New, updateResult.getBody().getStatus().getId());

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

        Assertions.assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = new UpdateInformationRequest(createResult.getBody().getInformation().getId(),
                "newTitle",
                "newDescription",
                "newInformation",
                new BigDecimal(50));

        var result = restTemplate.exchange("/api/information", HttpMethod.PUT, new HttpEntity<>(updateRequest), InformationResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void information_update_cantUpdateOthersInformation() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);
        var otherUser = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = getUpdateInformation(createResult.getBody().getInformation().getId());

        var result = putAuthorized(restTemplate,
                "/api/information",
                otherUser,
                updateRequest,
                ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(ApiErrors.Information.AccessViolation, result.getBody());
    }

    @Test
    void information_update_updateNotFound() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var updateRequest = getUpdateInformation(123123L);

        var result = putAuthorized(restTemplate,
                "/api/information",
                user,
                updateRequest,
                ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(ApiErrors.Information.NotFound, result.getBody());
    }


    @Test
    void information_update_allFieldsValidation() {
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
    void information_delete() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var getBeforeDelete = TestUtil.getAuthorized(restTemplate,
                "/api/information/" + result.getBody().getInformation().getId().toString(),
                user,
                InformationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, getBeforeDelete.getStatusCode());
        Assertions.assertEquals(result.getBody().getInformation().getId(), getBeforeDelete.getBody().getInformation().getId());

        var deleteResult = TestUtil.deleteAuthorized(restTemplate,
                "/api/information/" + result.getBody().getInformation().getId().toString(),
                user,
                InformationResponse[].class);

        Assertions.assertEquals(HttpStatus.OK, deleteResult.getStatusCode());

        var getAfterDelete = TestUtil.getAuthorized(restTemplate,
                "/api/information/" + result.getBody().getInformation().getId().toString(),
                user,
                ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, getAfterDelete.getStatusCode());
        Assertions.assertEquals(ApiErrors.Information.NotFound, getAfterDelete.getBody());

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

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var deleteResult = restTemplate.getForEntity(
                "/api/information/" + result.getBody().getInformation().getId().toString(),
                ErrorResponse.class
        );

        assertUnauthorized(deleteResult);
    }

    @Test
    void information_delete_accessViolationError() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);
        var user2 = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var deleteResult = TestUtil.deleteAuthorized(restTemplate,
                "/api/information/" + result.getBody().getInformation().getId().toString(),
                user2,
                ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, deleteResult.getStatusCode());
        Assertions.assertEquals(ApiErrors.Information.AccessViolation, deleteResult.getBody());
    }


    @Test
    void information_getAvailable() {
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                informer,
                createInformation,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var stalkerResult = TestUtil.getAuthorized(restTemplate,
                "/api/information/available",
                stalker,
                InformationResponse[].class);

        Assertions.assertEquals(HttpStatus.OK, stalkerResult.getStatusCode());
        Assertions.assertTrue(stalkerResult.getBody().length > 0);
        Assertions.assertTrue(Arrays.stream(stalkerResult.getBody())
                .anyMatch(info -> info.getInformation().getId().equals(result.getBody().getInformation().getId())));
        Assertions.assertTrue(Arrays.stream(stalkerResult.getBody()).allMatch(info ->
                info.getInformation().getInformation() == null));

        var clientResult = TestUtil.getAuthorized(restTemplate,
                "/api/information/available",
                client,
                InformationResponse[].class);
        Assertions.assertEquals(HttpStatus.OK, clientResult.getStatusCode());
        Assertions.assertEquals(0, clientResult.getBody().length);
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
        Assertions.assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var result = restTemplate.getForEntity("/api/information/available", ErrorResponse.class);
        assertUnauthorized(result);
    }

    @Test
    void information_request_fullProcess() {
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var informer2 = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                informer,
                createInformation,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var requestResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/request/" + result.getBody().getInformation().getId(),
                stalker,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, requestResult.getStatusCode());
        Assertions.assertEquals(StatusIds.Requested, requestResult.getBody().getInformation().getStatusId());
        Assertions.assertNotNull(requestResult.getBody().getRequestedUser());
        Assertions.assertEquals(stalker.getUser().getId(), requestResult.getBody().getRequestedUser().getId());
        Assertions.assertNull(requestResult.getBody().getAcquiredUser());

        var getRequestedResult = TestUtil.getAuthorized(restTemplate,
                "/api/information/requested",
                informer,
                InformationResponse[].class);
        Assertions.assertEquals(HttpStatus.OK, getRequestedResult.getStatusCode());
        Assertions.assertTrue(getRequestedResult.getBody().length > 0);
        Assertions.assertTrue(Arrays.stream(getRequestedResult.getBody()).anyMatch(i ->
                i.getInformation().getId().equals(result.getBody().getInformation().getId())));

        var getStalkerRequestedResult = TestUtil.getAuthorized(restTemplate,
                "/api/information/requested",
                stalker,
                InformationResponse[].class);
        Assertions.assertEquals(HttpStatus.OK, getStalkerRequestedResult.getStatusCode());
        Assertions.assertTrue(getStalkerRequestedResult.getBody().length > 0);
        Assertions.assertTrue(Arrays.stream(getStalkerRequestedResult.getBody()).anyMatch(i ->
                i.getInformation().getId().equals(result.getBody().getInformation().getId())));
        Assertions.assertTrue(Arrays.stream(getStalkerRequestedResult.getBody()).anyMatch(i ->
                i.getInformation().getInformation() == null));

        var getEmptyRequestedResult = TestUtil.getAuthorized(restTemplate,
                "/api/information/requested",
                informer2,
                InformationResponse[].class);
        Assertions.assertEquals(HttpStatus.OK, getEmptyRequestedResult.getStatusCode());
        Assertions.assertEquals(0, getEmptyRequestedResult.getBody().length);

        var getAvailableResult = TestUtil.getAuthorized(restTemplate,
                "/api/information/available",
                stalker,
                InformationResponse[].class);

        Assertions.assertEquals(HttpStatus.OK, getAvailableResult.getStatusCode());
        Assertions.assertFalse(Arrays.stream(getAvailableResult.getBody()).anyMatch(i ->
                i.getInformation().getId().equals(result.getBody().getInformation().getId())));
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
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var requestResult = restTemplate.postForEntity(
                "/api/information/request/" + result.getBody().getInformation().getId(),
                null,
                InformationResponse.class);

        assertUnauthorized(requestResult);
    }

    @Test
    void information_getRequested_unauthorizedError() {

        var result = restTemplate.getForEntity("/api/information/requested", ErrorResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void information_confirm_fullProcess_fullErrors() {
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var informer2 = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                informer,
                createInformation,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var requestResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/request/" + result.getBody().getInformation().getId(),
                stalker,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, requestResult.getStatusCode());
        Assertions.assertEquals(StatusIds.Requested, requestResult.getBody().getInformation().getStatusId());

        var errorConfirmResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/confirm/" + result.getBody().getInformation().getId(),
                informer2,
                ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, errorConfirmResult.getStatusCode());
        Assertions.assertEquals(ApiErrors.Information.AccessViolation, errorConfirmResult.getBody());

        var notFoundResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/confirm/" +123123123,
                informer,
                ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
        Assertions.assertEquals(ApiErrors.Information.NotFound, notFoundResult.getBody());

        var confirmResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/confirm/" + result.getBody().getInformation().getId(),
                informer,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, confirmResult.getStatusCode());
        Assertions.assertEquals(StatusIds.Acquired, confirmResult.getBody().getInformation().getStatusId());
        Assertions.assertEquals(stalker.getUser().getId(), confirmResult.getBody().getAcquiredUser().getId());
        Assertions.assertNull(confirmResult.getBody().getRequestedUser());

        var stalkerGetAcquired = TestUtil.getAuthorized(restTemplate,
                "/api/information",
                stalker,
                InformationResponse[].class);
        Assertions.assertEquals(HttpStatus.OK, stalkerGetAcquired.getStatusCode());
        Assertions.assertTrue(stalkerGetAcquired.getBody().length > 0);
        Assertions.assertTrue(Arrays.stream(stalkerGetAcquired.getBody()).allMatch(i ->
                i.getInformation().getInformation() != null));
        Assertions.assertTrue(Arrays.stream(stalkerGetAcquired.getBody()).anyMatch(i ->
                i.getInformation().getId().equals(result.getBody().getInformation().getId())));
    }

    @Test
    void information_confirm_unauthorizedError() {

        var result = restTemplate.postForEntity("/api/information/confirm/10",
                null,
                ErrorResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void information_decline_fullProcess_fullErrors() {
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var informer2 = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                informer,
                createInformation,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        var requestResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/request/" + result.getBody().getInformation().getId(),
                stalker,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, requestResult.getStatusCode());
        Assertions.assertEquals(StatusIds.Requested, requestResult.getBody().getInformation().getStatusId());

        var errorConfirmResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/decline/" + result.getBody().getInformation().getId(),
                informer2,
                ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, errorConfirmResult.getStatusCode());
        Assertions.assertEquals(ApiErrors.Information.AccessViolation, errorConfirmResult.getBody());

        var notFoundResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/decline/" +123123123,
                informer,
                ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());
        Assertions.assertEquals(ApiErrors.Information.NotFound, notFoundResult.getBody());

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/information/decline/" + result.getBody().getInformation().getId(),
                informer,
                InformationResponse.class);
        Assertions.assertEquals(HttpStatus.OK, declineResult.getStatusCode());
        Assertions.assertEquals(StatusIds.New, declineResult.getBody().getInformation().getStatusId());
        Assertions.assertNull(declineResult.getBody().getRequestedUser());
        Assertions.assertNull(declineResult.getBody().getAcquiredUser());

        var stalkerGetAvailable = TestUtil.getAuthorized(restTemplate,
                "/api/information/available",
                stalker,
                InformationResponse[].class);
        Assertions.assertEquals(HttpStatus.OK, stalkerGetAvailable.getStatusCode());
        Assertions.assertTrue(stalkerGetAvailable.getBody().length > 0);
        Assertions.assertTrue(Arrays.stream(stalkerGetAvailable.getBody()).allMatch(i ->
                i.getInformation().getInformation() == null));
        Assertions.assertTrue(Arrays.stream(stalkerGetAvailable.getBody()).anyMatch(i ->
                i.getInformation().getId().equals(result.getBody().getInformation().getId())));

        var stalkerGetAcquired = TestUtil.getAuthorized(restTemplate,
                "/api/information",
                stalker,
                InformationResponse[].class);

        Assertions.assertEquals(HttpStatus.OK, stalkerGetAcquired.getStatusCode());
        Assertions.assertEquals(0, stalkerGetAcquired.getBody().length);
    }

    @Test
    void information_decline_unauthorizedError() {
        var result = restTemplate.postForEntity("/api/information/decline/10",
                null,
                ErrorResponse.class);

        assertUnauthorized(result);
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

        assertValidationError(result);
    }

    private void updateValidationHelper(Consumer<UpdateInformationRequest> func) {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);
        var createInformation = getCreateInformation();
        var createResult = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                InformationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, createResult.getStatusCode());

        var updateRequest = getUpdateInformation(createResult.getBody().getInformation().getId());
        func.accept(updateRequest);

        var result = putAuthorized(restTemplate,
                "/api/information",
                user,
                updateRequest,
                ErrorResponse.class);

        assertValidationError(result);
    }

    private CreateInformationRequest getCreateInformation() {
        return new CreateInformationRequest(
                "title",
                "description",
                "information",
                new BigDecimal(100));
    }


    private UpdateInformationRequest getUpdateInformation(Long id) {
        return new UpdateInformationRequest(id,
                "newTitle",
                "newDescription",
                "newInformation",
                new BigDecimal(50));
    }

}
