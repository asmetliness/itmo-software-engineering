package com.artefact.api.functional;


import com.artefact.api.ApiApplication;
import com.artefact.api.model.Notification;
import com.artefact.api.repository.*;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.response.NotificationResponse;
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

import static com.artefact.api.utils.TestUtil.*;

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
    void notifications_wasRead() {
        var authInfo = register(restTemplate);
        var authInfo2 = register(restTemplate);

        createNotification(authInfo, "test");

        var result = getAuthorized(
                restTemplate,
                "/api/notifications",
                authInfo,
                NotificationResponse[].class);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(1, result.getBody().length);
        Assertions.assertEquals("test", result.getBody()[0].getText());
        Assertions.assertFalse(result.getBody()[0].isWasRead());

        var result2 = getAuthorized(
                restTemplate,
                "/api/notifications",
                authInfo,
                NotificationResponse[].class);

        Assertions.assertTrue(result2.getBody()[0].isWasRead());

        var result3 = getAuthorized(
                restTemplate,
                "/api/notifications",
                authInfo2,
                NotificationResponse[].class
        );

        Assertions.assertEquals(HttpStatus.OK, result3.getStatusCode());
        Assertions.assertNotNull(result3.getBody());
        Assertions.assertEquals(0, result3.getBody().length);
    }


    private void createNotification(AuthResponse response, String message) {
        var notification = new Notification();
        notification.setMessage(message);
        notification.setUserId(response.getUser().getId());

        notificationRepository.save(notification);
    }


}
