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
    void information_create_emptyTitleError() {
        createValidationHelper((info) -> {
            info.setTitle(null);
        });
    }

    @Test
    void information_create_emptyDescriptionError() {
        createValidationHelper((info) -> {
            info.setDescription(null);
        });
    }

    @Test
    void information_create_emptyInformationError() {
        createValidationHelper((info) -> {
            info.setInformation(null);
        });
    }

    @Test
    void information_create_emptyPriceError() {
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
    void information_update_emptyIdError() {
        updateValidationHelper((info) -> {
            info.setId(null);
        });
    }

    @Test
    void information_update_emptyTitleError() {
        updateValidationHelper((info) -> {
            info.setTitle(null);
        });
    }

    @Test
    void information_update_emptyDescriptionError() {
        updateValidationHelper((info) -> {
            info.setDescription(null);
        });
    }

    @Test
    void information_update_emptyInformationError() {
        updateValidationHelper((info) -> {
            info.setInformation(null);
        });
    }

    @Test
    void information_update_emptyPriceError() {
        updateValidationHelper((info) -> {
            info.setPrice(null);
        });
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
