package com.artefact.api.security;

import com.artefact.api.ApiApplication;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.utils.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static com.artefact.api.utils.TestUtil.assertUnauthorized;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class ArtifactModuleTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void artifacts_getAll_unauthorizedError() {

        var artifacts = TestUtil.getAuthorized(restTemplate,
                "/api/artifacts",
                null,
                ErrorResponse.class);

        assertUnauthorized(artifacts);
    }
}
