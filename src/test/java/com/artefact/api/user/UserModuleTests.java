package com.artefact.api.user;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.UpdateUserRequest;
import com.artefact.api.response.UserResponse;
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

import static com.artefact.api.utils.TestUtil.assertUnauthorized;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class UserModuleTests {


    @Autowired
    private TestRestTemplate restTemplate;

    @AfterAll
    static void cleanupData(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @Test
    void users_getCurrent() {
        var user = TestUtil.register(restTemplate);

        var result = TestUtil.getAuthorized(restTemplate,
                "/api/users/current",
                user,
                UserResponse.class);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(user.getUser().getId(), result.getBody().getUser().getId());
    }

    @Test
    void users_getCurrent_unauthorizedError() {

        var result = restTemplate.getForEntity("/api/users/current", UserResponse.class);
        assertUnauthorized(result);
    }

    @Test
    void users_updateCurrent() {
        var user = TestUtil.register(restTemplate);

        var updateReq = new UpdateUserRequest(
                "firstName",
                "lastName",
                "middleName",
                "nickname"
        );

        var result = TestUtil.putAuthorized(restTemplate,
                "/api/users/current",
                user,
                updateReq,
                UserResponse.class);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(user.getUser().getId(), result.getBody().getUser().getId());
        Assertions.assertEquals(updateReq.getFirstName(), result.getBody().getUser().getFirstName());
        Assertions.assertEquals(updateReq.getLastName(), result.getBody().getUser().getLastName());
        Assertions.assertEquals(updateReq.getMiddleName(), result.getBody().getUser().getMiddleName());
        Assertions.assertEquals(updateReq.getNickname(), result.getBody().getUser().getNickname());
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

        assertUnauthorized(result);
    }


    @Test
    void users_getStalkers() {
        TestUtil.registerRole(restTemplate, Role.Stalker);
        TestUtil.registerRole(restTemplate, Role.Stalker);

        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);
        var notHuckster = TestUtil.registerRole(restTemplate, Role.Client);

        var hucksterResult = TestUtil.getAuthorized(restTemplate,
                "/api/users/stalkers",
                huckster,
                User[].class);

        Assertions.assertEquals(HttpStatus.OK, hucksterResult.getStatusCode());
        Assertions.assertNotNull(hucksterResult.getBody());
        Assertions.assertEquals(2, hucksterResult.getBody().length);

        var notHucksterResult = TestUtil.getAuthorized(restTemplate,
                "/api/users/stalkers",
                notHuckster,
                User[].class);

        Assertions.assertEquals(HttpStatus.OK, notHucksterResult.getStatusCode());
        Assertions.assertNotNull(notHucksterResult.getBody());
        Assertions.assertEquals(0, notHucksterResult.getBody().length);
    }

    @Test
    void users_getStalkers_unauthorizedError() {
        var result = restTemplate.getForEntity("/api/users/stalkers", UserResponse.class);
        assertUnauthorized(result);
    }

    @Test
    void users_getCouriers() {

        TestUtil.registerRole(restTemplate, Role.Courier);
        TestUtil.registerRole(restTemplate, Role.Courier);

        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var hucksterResult = TestUtil.getAuthorized(restTemplate,
                "/api/users/couriers",
                huckster,
                User[].class);

        Assertions.assertEquals(HttpStatus.OK, hucksterResult.getStatusCode());
        Assertions.assertNotNull(hucksterResult.getBody());
        Assertions.assertEquals(2, hucksterResult.getBody().length);

        var weaponDealerResult = TestUtil.getAuthorized(restTemplate,
                "/api/users/couriers",
                weaponDealer,
                User[].class);

        Assertions.assertEquals(HttpStatus.OK, weaponDealerResult.getStatusCode());
        Assertions.assertNotNull(weaponDealerResult.getBody());
        Assertions.assertEquals(2, weaponDealerResult.getBody().length);


        var clientResult = TestUtil.getAuthorized(restTemplate,
                "/api/users/couriers",
                client,
                User[].class);

        Assertions.assertEquals(HttpStatus.OK, clientResult.getStatusCode());
        Assertions.assertNotNull(clientResult.getBody());
        Assertions.assertEquals(0, clientResult.getBody().length);
    }

    @Test
    void users_getCouriers_unauthorizedError() {
        var result = restTemplate.getForEntity("/api/users/couriers", UserResponse.class);
        assertUnauthorized(result);
    }

}
