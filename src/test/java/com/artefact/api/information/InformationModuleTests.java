package com.artefact.api.information;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.repository.InformationRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.CreateInformationRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static com.artefact.api.utils.TestUtil.assertUnauthorized;
import static com.artefact.api.utils.TestUtil.assertValidationError;

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
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();
        createInformation.setTitle(null);
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                ErrorResponse.class);

        assertValidationError(result);
    }

    @Test
    void information_create_emptyDescriptionError() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();
        createInformation.setDescription(null);
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                ErrorResponse.class);

        assertValidationError(result);
    }

    @Test
    void information_create_emptyInformationError() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();
        createInformation.setInformation(null);
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
                ErrorResponse.class);

        assertValidationError(result);
    }

    @Test
    void information_create_emptyPriceError() {
        var user = TestUtil.registerRole(restTemplate, Role.Informer);

        var createInformation = getCreateInformation();
        createInformation.setPrice(null);
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/information",
                user,
                createInformation,
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

}
