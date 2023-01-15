package com.artefact.api.order;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.repository.ArtifactRepository;
import com.artefact.api.repository.NotificationRepository;
import com.artefact.api.repository.OrderRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.CreateOrderRequest;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.response.NotificationResponse;
import com.artefact.api.response.OrderResponse;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

import static com.artefact.api.utils.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ApiApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "POSTGRESQL_DB_HOST=localhost"
})
@ActiveProfiles("test")
public class OrderModuleTests {


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ArtifactRepository artifactRepository;

    @AfterAll
    static void cleanupData(
            @Autowired NotificationRepository notificationRepository,
            @Autowired UserRepository userRepository,
            @Autowired OrderRepository orderRepository) {
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    void order_create() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateRequest();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(request.getArtifactId(), result.getBody().getArtifact().getId());
        assertEquals(request.getDeliveryAddress(), result.getBody().getOrder().getDeliveryAddress());
        assertEquals(request.getPrice(), result.getBody().getOrder().getPrice());
        assertEquals(StatusIds.New, result.getBody().getOrder().getStatusId());
        assertEquals(client.getUser().getId(), result.getBody().getOrder().getCreatedUserId());
        assertNull(result.getBody().getOrder().getAcceptedCourierId());
        assertNull(result.getBody().getOrder().getAcceptedUserId());
        assertNull(result.getBody().getOrder().getAssignedUserId());
        assertNull(result.getBody().getOrder().getSuggestedUserId());
    }

    @Test
    void order_create_unauthorizedError() {
        var request = getCreateRequest();

        var result = restTemplate.postForEntity("/api/orders", request, ErrorResponse.class);

        assertUnauthorized(result);
    }

    @Test
    void order_create_wrongRolesError() {
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);
        var weaponDealer = TestUtil.registerRole(restTemplate, Role.WeaponDealer);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);

        var roles = new AuthResponse[] {
                stalker, huckster, informer, weaponDealer, courier
        };

        for(int i = 0; i < roles.length; ++i) {

            var request = getCreateRequest();

            var result = TestUtil.postAuthorized(restTemplate,
                    "/api/orders",
                    roles[i],
                    request,
                    ErrorResponse.class);

            assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
            assertEquals(ApiErrors.Order.CantCreate, result.getBody());
        }
    }

    @Test
    void order_create_validationErrors() {
        orderTestHelper((request) -> {
            request.setArtifactId(null);
        });
        orderTestHelper((request) -> {
            request.setPrice(null);
        });
        orderTestHelper((request) -> {
            request.setDeliveryAddress(null);
        });
    }

    @Test
    void order_create_hucksterNotifications() {
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var hucksterNotifications  = getAuthorized(
                restTemplate,
                "/api/notifications",
                huckster,
                NotificationResponse[].class);

        assertEquals(HttpStatus.OK, hucksterNotifications.getStatusCode());
        assertNotNull(hucksterNotifications.getBody());
        assertTrue(hucksterNotifications.getBody().length > 0);
        assertTrue(Arrays.stream(hucksterNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId())));


    }

    void orderTestHelper(Consumer<CreateOrderRequest> func) {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateRequest();
        func.accept(request);
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                ErrorResponse.class);

        assertValidationError(result);
    }


    private CreateOrderRequest getCreateRequest() {

        var artifact = artifactRepository.findAll().get(0);

        var createRequest = new CreateOrderRequest(artifact.getId(),
                artifact.getPrice(),
                new Date(),
                "spb");

        return createRequest;
    }
}
