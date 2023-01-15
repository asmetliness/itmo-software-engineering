package com.artefact.api.order;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Order;
import com.artefact.api.model.User;
import com.artefact.api.repository.ArtifactRepository;
import com.artefact.api.repository.NotificationRepository;
import com.artefact.api.repository.OrderRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.CreateOrderRequest;
import com.artefact.api.request.SuggestOrderRequest;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.response.ErrorResponse;
import com.artefact.api.response.NotificationResponse;
import com.artefact.api.response.OrderResponse;
import com.artefact.api.utils.ApiErrors;
import com.artefact.api.utils.NotificationMessages;
import com.artefact.api.utils.TestUtil;
import jdk.jshell.spi.ExecutionControl;
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
import java.util.Objects;
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

        assertOK(result);
        assertEquals(request.getArtifactId(), Objects.requireNonNull(result.getBody()).getArtifact().getId());
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

        for (AuthResponse role : roles) {

            var request = getCreateRequest();

            var result = TestUtil.postAuthorized(restTemplate,
                    "/api/orders",
                    role,
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
        assertOK(result);

        var hucksterNotifications  = getAuthorized(
                restTemplate,
                "/api/notifications",
                huckster,
                NotificationResponse[].class);

        assertOK(result);
        assertTrue(Objects.requireNonNull(hucksterNotifications.getBody()).length > 0);
        assertTrue(Arrays.stream(hucksterNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(Objects.requireNonNull(result.getBody()).getOrder().getId())
                && n.getText().equals(NotificationMessages.Order.Created)
        ));

    }

    @Test
    void order_getById() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        assertOK(result);

        var getResult = TestUtil.getAuthorized(restTemplate,
                "/api/orders/" + result.getBody().getOrder().getId(),
                client,
                OrderResponse.class);

        assertOK(result);
        assertEquals(result.getBody().getOrder().getId(), getResult.getBody().getOrder().getId());
    }

    @Test
    void order_getById_unauthorizedError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        assertOK(result);

        var getResult = restTemplate.getForEntity(
                "/api/orders/" + result.getBody().getOrder().getId(),
                ErrorResponse.class
        );
        assertUnauthorized(getResult);
    }

    @Test
    void order_getById_notFoundError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        assertOK(result);

        var getResult = TestUtil.getAuthorized(restTemplate,
                "/api/orders/" + 1231232,
                client,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, getResult.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, getResult.getBody());
    }

    @Test
    void order_getAll_client() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var ordersResult = TestUtil.getAuthorized(
                restTemplate,
                "/api/orders",
                client,
                OrderResponse[].class
        );
        assertOK(ordersResult);
        assertTrue(Objects.requireNonNull(ordersResult.getBody()).length > 0);
        assertTrue(Arrays.stream(ordersResult.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(Objects.requireNonNull(result.getBody()).getOrder().getId())));
    }

    @Test
    void order_getAll_stalker() {
        assertTrue(false);
    }

    @Test
    void order_getAll_default() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);

        var request = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        var ordersResult = TestUtil.getAuthorized(
                restTemplate,
                "/api/orders",
                informer,
                OrderResponse[].class
        );
        assertOK(ordersResult);
        assertEquals(0, Objects.requireNonNull(ordersResult.getBody()).length);
    }


    @Test
    void order_getAll_unauthorizedError() {
        var ordersResult = TestUtil.getAuthorized(
                restTemplate,
                "/api/orders",
                null,
                ErrorResponse.class
        );
        assertUnauthorized(ordersResult);
    }


    @Test
    void order_getAvailable_huckster() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var request = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);

        assertOK(result);

        var hucksterAvailable = TestUtil.getAuthorized(restTemplate,
                "/api/orders/available",
                huckster,
                OrderResponse[].class);

        assertOK(hucksterAvailable);
        assertTrue(Objects.requireNonNull(hucksterAvailable.getBody()).length > 0);
        assertTrue(Arrays.stream(hucksterAvailable.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(Objects.requireNonNull(result.getBody()).getOrder().getId())));
    }

    @Test
    void order_getAvailable_stalker() {
        assertTrue(false);
    }

    @Test
    void order_getAvailable_courier() {
        assertTrue(false);
    }

    @Test
    void order_getAvailable_default() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);

        var request = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);

        assertOK(result);

        var informerAvailable = TestUtil.getAuthorized(restTemplate,
                "/api/orders/available",
                informer,
                OrderResponse[].class);

        assertOK(informerAvailable);
        assertEquals(0, Objects.requireNonNull(informerAvailable.getBody()).length);

    }
    @Test
    void order_getAvailable_unauthorizedError() {
        var ordersResult = TestUtil.getAuthorized(
                restTemplate,
                "/api/orders/available",
                null,
                ErrorResponse.class
        );
        assertUnauthorized(ordersResult);
    }


    @Test
    void order_accept_huckster() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);
        assertEquals(result.getBody().getOrder().getId(), acceptResult.getBody().getOrder().getId());
        assertEquals(StatusIds.AcceptedByHuckster, acceptResult.getBody().getOrder().getStatusId());
        assertEquals(huckster.getUser().getId(), acceptResult.getBody().getAcceptedUser().getId());
        assertNull(acceptResult.getBody().getAssignedUser());
        assertNull(acceptResult.getBody().getSuggestedUser());
    }

    @Test
    void order_accept_huckster_clientNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);

        var clientNotifications = TestUtil.getAuthorized(restTemplate,
                "/api/notifications",
                client,
                NotificationResponse[].class);

        assertOK(clientNotifications);
        assertTrue(Arrays.stream(clientNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(acceptResult.getBody().getOrder().getId())
                        && n.getText().equals(NotificationMessages.Order.AcceptedByHuckster)
        ));
    }

    @Test
    void order_getAll_huckster() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);

        var getAllResult = TestUtil.getAuthorized(restTemplate,
                "/api/orders",
                huckster,
                OrderResponse[].class);
        assertOK(getAllResult);

        assertTrue(Objects.requireNonNull(getAllResult.getBody()).length > 0);
        assertTrue(Arrays.stream(getAllResult.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(Objects.requireNonNull(result.getBody()).getOrder().getId())));
    }

    @Test
    void order_accept_stalker() {
        assertTrue(false);
    }

    @Test
    void order_accept_courier() {
        assertTrue(false);
    }

    @Test
    void order_accept_notFound() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + 13213123,
                huckster,
                ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, acceptResult.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, acceptResult.getBody());
    }

    @Test
    void order_accept_unauthorized() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                null,
                ErrorResponse.class);

        assertUnauthorized(acceptResult);
    }

    @Test
    void order_decline_huckster() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(declineResult);
        assertEquals(result.getBody().getOrder().getId(), declineResult.getBody().getOrder().getId());
        assertEquals(StatusIds.New, declineResult.getBody().getOrder().getStatusId());
        assertNull(declineResult.getBody().getAcceptedUser());
        assertNull(declineResult.getBody().getAssignedUser());
        assertNull(declineResult.getBody().getSuggestedUser());

    }

    @Test
    void order_decline_huckster_clientNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(declineResult);

        var clientNotifications = TestUtil.getAuthorized(restTemplate,
                "/api/notifications",
                client,
                NotificationResponse[].class);

        assertOK(clientNotifications);
        assertTrue(Arrays.stream(clientNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(acceptResult.getBody().getOrder().getId())
                        && n.getText().equals(NotificationMessages.Order.DeclinedByHuckster)
        ));
    }

    @Test
    void order_decline_stalker() {
        assertTrue(false);
    }
    @Test
    void order_decline_courier() {
        assertTrue(false);
    }

    @Test
    void order_decline_notFound() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + 13213123,
                huckster,
                ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, declineResult.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, declineResult.getBody());
    }

    @Test
    void order_decline_unauthorizedError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + result.getBody().getOrder().getId(),
                null,
                ErrorResponse.class);

        assertUnauthorized(declineResult);
    }


    @Test
    void order_suggest_toStalker() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        assertOK(suggestResult);
        assertNotNull(suggestResult.getBody().getSuggestedUser());
        assertEquals(stalker.getUser().getId(), suggestResult.getBody().getSuggestedUser().getId());
        assertEquals(StatusIds.AcceptedByHuckster, suggestResult.getBody().getOrder().getStatusId());
        assertNull(suggestResult.getBody().getAssignedUser());
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

        assertUnauthorized(suggestResult);
    }

    @Test
    void order_suggest_notFoundError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        suggestRequest.setOrderId(123123L);
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, suggestResult.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, suggestResult.getBody());
    }

    @Test
    void order_suggest_wrongUserError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());

        var suggestResultWrongRole = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                client,
                suggestRequest,
                ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, suggestResultWrongRole.getStatusCode());
        assertEquals(ApiErrors.Order.AccessError, suggestResultWrongRole.getBody());

        suggestRequest.setUserId(123123L);
        var suggestResultNotFound = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, suggestResultNotFound.getStatusCode());
        assertEquals(ApiErrors.Order.CantSuggestToUser, suggestResultNotFound.getBody());

        suggestRequest.setUserId(client.getUser().getId());
        var suggestResultWrongUser = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, suggestResultWrongUser.getStatusCode());
        assertEquals(ApiErrors.Order.CantSuggestToUser, suggestResultWrongUser.getBody());



    }




    void orderSuggestHelper(Consumer<SuggestOrderRequest> func) {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        func.accept(suggestRequest);

        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                ErrorResponse.class);
        assertValidationError(suggestResult);
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

        return new CreateOrderRequest(artifact.getId(),
                artifact.getPrice(),
                new Date(),
                "spb");
    }

    private SuggestOrderRequest getSuggestRequest(User user, Order order) {

        return new SuggestOrderRequest(order.getId(), user.getId());
    }
}
