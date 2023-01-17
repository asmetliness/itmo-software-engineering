package com.artefact.api.security;

import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.model.Order;
import com.artefact.api.model.User;
import com.artefact.api.repository.*;
import com.artefact.api.request.CreateOrderRequest;
import com.artefact.api.request.SuggestOrderRequest;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.response.OrderResponse;
import com.artefact.api.utils.TestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;

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
    void order_create_unauthorizedError() {
        var request = getCreateOrderRequest();

        var result = restTemplate.postForEntity("/api/orders", request, ErrorResponse.class);

        TestUtil.assertUnauthorized(result);
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
    void order_getById_unauthorizedError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        TestUtil.assertOK(result);

        var getResult = restTemplate.getForEntity(
                "/api/orders/" + result.getBody().getOrder().getId(),
                ErrorResponse.class
        );
        TestUtil.assertUnauthorized(getResult);
    }

    @Test
    void order_getAll_unauthorizedError() {
        var ordersResult = TestUtil.getAuthorized(
                restTemplate,
                "/api/orders",
                null,
                ErrorResponse.class
        );
        TestUtil.assertUnauthorized(ordersResult);
    }

    @Test
    void order_getAvailable_unauthorizedError() {
        var ordersResult = TestUtil.getAuthorized(
                restTemplate,
                "/api/orders/available",
                null,
                ErrorResponse.class
        );
        TestUtil.assertUnauthorized(ordersResult);
    }

    @Test
    void order_accept_unauthorized() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                null,
                ErrorResponse.class);

        TestUtil.assertUnauthorized(acceptResult);
    }

    @Test
    void order_decline_unauthorizedError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                null,
                ErrorResponse.class);

        TestUtil.assertUnauthorized(declineResult);
    }


    @Test
    void order_suggest_validationError() {
        orderSuggestHelper(s -> {
            s.setOrderId(null);
        });
        orderSuggestHelper(s -> {
            s.setUserId(null);
        });
    }

    @Test
    void order_suggest_unauthorizedError() {
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                null,
                null,
                ErrorResponse.class);

        TestUtil.assertUnauthorized(suggestResult);
    }

    @Test
    void order_start_unauthorizedError() {
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders/start/123",
                null,
                ErrorResponse.class);
        TestUtil.assertUnauthorized(result);
    }

    @Test
    void order_complete_unauthorizedError() {
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders/complete/123",
                null,
                ErrorResponse.class);

        TestUtil.assertUnauthorized(result);
    }

    @Test
    void order_deliver_unauthorizedError() {
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders/deliver/1232",
                null,
                ErrorResponse.class);

        TestUtil.assertUnauthorized(result);
    }

    void orderSuggestHelper(Consumer<SuggestOrderRequest> func) {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        func.accept(suggestRequest);

        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                ErrorResponse.class);
        TestUtil.assertValidationError(suggestResult);
    }

    private SuggestOrderRequest getSuggestRequest(User user, Order order) {

        return new SuggestOrderRequest(order.getId(), user.getId());
    }

    void orderTestHelper(Consumer<CreateOrderRequest> func) {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateOrderRequest();
        func.accept(request);
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                ErrorResponse.class);

        TestUtil.assertValidationError(result);
    }

    private CreateOrderRequest getCreateOrderRequest() {

        var artifact = artifactRepository.findAll().get(0);

        return new CreateOrderRequest(artifact.getId(),
                artifact.getPrice(),
                new Date(),
                "spb");
    }
}
