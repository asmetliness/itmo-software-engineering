package com.artefact.api.security;

import com.artefact.api.ApiApplication;
import com.artefact.api.repository.NotificationRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.response.NotificationResponse;
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
public class NotificationModuleTests {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void notification_get_unauthorizedError()
    {
        var result = this.restTemplate.getForEntity("/api/notifications", NotificationResponse[].class);
        assertUnauthorized(result);
    }

    @Test
    void notification_getNew_unauthorizedError()
    {
        var result = this.restTemplate.getForEntity("/api/notifications/new", NotificationResponse[].class);
        assertUnauthorized(result);
    }
}
