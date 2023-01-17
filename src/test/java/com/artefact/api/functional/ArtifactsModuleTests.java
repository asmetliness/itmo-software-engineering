package com.artefact.api.functional;

import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.model.Artifact;
import com.artefact.api.repository.*;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.utils.TestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static com.artefact.api.utils.TestUtil.assertOK;
import static com.artefact.api.utils.TestUtil.assertUnauthorized;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class ArtifactsModuleTests {

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
    void artifacts_getAll() {

        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var artifacts = TestUtil.getAuthorized(restTemplate,
                "/api/artifacts",
                client,
                Artifact[].class);

        assertOK(artifacts);
        Assertions.assertTrue(artifacts.getBody().length > 0);
    }

}
