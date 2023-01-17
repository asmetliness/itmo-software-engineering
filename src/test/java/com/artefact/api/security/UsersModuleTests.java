package com.artefact.api.security;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.UpdateUserRequest;
import com.artefact.api.response.UserResponse;
import com.artefact.api.utils.TestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class UsersModuleTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterAll
    static void cleanupData(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @Test
    void users_getCurrent_unauthorizedError() {

        var result = restTemplate.getForEntity("/api/users/current", UserResponse.class);
        TestUtil.assertUnauthorized(result);
    }

    @Test
    void users_updateCurrent_unauthorizedError() {

        var updateReq = new UpdateUserRequest(
                "firstName",
                "lastName",
                "middleName",
                "nickname"
        );
        var requestEntity = new HttpEntity<>(updateReq);
        var result = restTemplate.exchange("/api/users/current", HttpMethod.PUT, requestEntity, UserResponse.class);

        TestUtil.assertUnauthorized(result);
    }

    @Test
    void users_getStalkers_unauthorizedError() {
        var result = restTemplate.getForEntity("/api/users/stalkers", UserResponse.class);
        TestUtil.assertUnauthorized(result);
    }

    @Test
    void users_getCouriers_unauthorizedError() {
        var result = restTemplate.getForEntity("/api/users/couriers", UserResponse.class);
        TestUtil.assertUnauthorized(result);
    }


    @Test
    void user_uploadImage_unauthorizedError() throws IOException {
        var file = "image.png";

        var result = TestUtil.postFile(restTemplate,
                "/api/users/image/upload",
                null,
                file,
                UserResponse.class);

        TestUtil.assertUnauthorized(result);
    }

    @Test
    void user_deleteImage_unauthorizedError() {

        var result = TestUtil.deleteAuthorized(restTemplate,
                "/api/users/image/delete",
                null,
                UserResponse.class);
        TestUtil.assertUnauthorized(result);
    }
}
