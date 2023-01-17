package com.artefact.api.functional;


import com.artefact.api.ApiApplication;
import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.utils.TestUtil;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;

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

        var request = getCreateOrderRequest();

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);

        TestUtil.assertOK(result);
        assertEquals(request.getArtifactId(), Objects.requireNonNull(result.getBody()).getArtifact().getId());
        assertEquals(request.getDeliveryAddress(), result.getBody().getOrder().getDeliveryAddress());
        assertEquals(request.getPrice(), result.getBody().getOrder().getPrice());
        assertNotNull(result.getBody().getOrder().getCompletionDate());
        assertEquals(StatusIds.New, result.getBody().getOrder().getStatusId());
        assertEquals(client.getUser().getId(), result.getBody().getOrder().getCreatedUserId());

        assertNull(result.getBody().getOrder().getAcceptedCourierId());
        assertNull(result.getBody().getOrder().getAcceptedUserId());
        assertNull(result.getBody().getOrder().getAssignedUserId());
        assertNull(result.getBody().getOrder().getSuggestedUserId());
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

            var request = getCreateOrderRequest();

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
    void order_create_hucksterNotifications() {
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        TestUtil.assertOK(result);

        var hucksterNotifications  = TestUtil.getAuthorized(
                restTemplate,
                "/api/notifications",
                huckster,
                NotificationResponse[].class);

        TestUtil.assertOK(result);
        assertTrue(Objects.requireNonNull(hucksterNotifications.getBody()).length > 0);
        assertTrue(Arrays.stream(hucksterNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(Objects.requireNonNull(result.getBody()).getOrder().getId())
                && n.getText().equals(NotificationMessages.Order.Created)
        ));

    }

    @Test
    void order_getById() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        TestUtil.assertOK(result);

        var getResult = TestUtil.getAuthorized(restTemplate,
                "/api/orders/" + result.getBody().getOrder().getId(),
                client,
                OrderResponse.class);

        TestUtil.assertOK(result);
        assertEquals(result.getBody().getOrder().getId(), getResult.getBody().getOrder().getId());
    }

    @Test
    void order_getById_notFoundError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var request = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);
        TestUtil.assertOK(result);

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

        var request = getCreateOrderRequest();
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
        TestUtil.assertOK(ordersResult);
        assertTrue(Objects.requireNonNull(ordersResult.getBody()).length > 0);
        assertTrue(Arrays.stream(ordersResult.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(Objects.requireNonNull(result.getBody()).getOrder().getId())));
    }

    @Test
    void order_getAll_default() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);

        var request = getCreateOrderRequest();
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
        TestUtil.assertOK(ordersResult);
        assertEquals(0, Objects.requireNonNull(ordersResult.getBody()).length);
    }

    @Test
    void order_getAvailable_huckster() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var request = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var hucksterAvailable = TestUtil.getAuthorized(restTemplate,
                "/api/orders/available",
                huckster,
                OrderResponse[].class);

        TestUtil.assertOK(hucksterAvailable);
        assertTrue(Objects.requireNonNull(hucksterAvailable.getBody()).length > 0);
        assertTrue(Arrays.stream(hucksterAvailable.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(Objects.requireNonNull(result.getBody()).getOrder().getId())));
    }

    @Test
    void order_getAvailable_default() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var informer = TestUtil.registerRole(restTemplate, Role.Informer);

        var request = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                request,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var informerAvailable = TestUtil.getAuthorized(restTemplate,
                "/api/orders/available",
                informer,
                OrderResponse[].class);

        TestUtil.assertOK(informerAvailable);
        assertEquals(0, Objects.requireNonNull(informerAvailable.getBody()).length);

    }

    @Test
    void order_accept_huckster_withNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);
        assertEquals(result.getBody().getOrder().getId(), Objects.requireNonNull(acceptResult.getBody()).getOrder().getId());
        assertEquals(StatusIds.AcceptedByHuckster, acceptResult.getBody().getOrder().getStatusId());
        assertEquals(huckster.getUser().getId(), acceptResult.getBody().getAcceptedUser().getId());
        assertNull(acceptResult.getBody().getAssignedUser());
        assertNull(acceptResult.getBody().getSuggestedUser());

        var clientNotifications = getNotifications(client);

        TestUtil.assertOK(clientNotifications);
        assertTrue(Arrays.stream(Objects.requireNonNull(clientNotifications.getBody())).anyMatch(n ->
                n.getOrderId().equals(acceptResult.getBody().getOrder().getId())
                        && n.getText().equals(NotificationMessages.Order.AcceptedByHuckster)
        ));
    }

    @Test
    void order_getAll_huckster() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var getAllResult = TestUtil.getAuthorized(restTemplate,
                "/api/orders",
                huckster,
                OrderResponse[].class);
        TestUtil.assertOK(getAllResult);

        assertTrue(Objects.requireNonNull(getAllResult.getBody()).length > 0);
        assertTrue(Arrays.stream(getAllResult.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(Objects.requireNonNull(result.getBody()).getOrder().getId())));
    }


    @Test
    void order_accept_notFound() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + 13213123,
                huckster,
                ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, acceptResult.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, acceptResult.getBody());
    }


    @Test
    void order_decline_huckster_withNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + result.getBody().getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(declineResult);
        assertEquals(result.getBody().getOrder().getId(), Objects.requireNonNull(declineResult.getBody()).getOrder().getId());
        assertEquals(StatusIds.New, declineResult.getBody().getOrder().getStatusId());
        assertNull(declineResult.getBody().getAcceptedUser());
        assertNull(declineResult.getBody().getAssignedUser());
        assertNull(declineResult.getBody().getSuggestedUser());


        var clientNotifications = getNotifications(client);

        TestUtil.assertOK(clientNotifications);
        assertTrue(Arrays.stream(Objects.requireNonNull(clientNotifications.getBody())).anyMatch(n ->
                n.getOrderId().equals(Objects.requireNonNull(acceptResult.getBody()).getOrder().getId())
                        && n.getText().equals(NotificationMessages.Order.DeclinedByHuckster)
        ));
    }



    @Test
    void order_decline_notFound() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var declineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + 13213123,
                huckster,
                ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, declineResult.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, declineResult.getBody());
    }


    @Test
    void order_suggest_toStalker_withNotifications() {
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
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);
        assertNotNull(Objects.requireNonNull(suggestResult.getBody()).getSuggestedUser());
        assertEquals(stalker.getUser().getId(), suggestResult.getBody().getSuggestedUser().getId());
        assertEquals(StatusIds.AcceptedByHuckster, suggestResult.getBody().getOrder().getStatusId());
        assertNull(suggestResult.getBody().getAssignedUser());

        var stalkerNotifications = getNotifications(stalker);
        TestUtil.assertOK(stalkerNotifications);
        assertTrue(Arrays.stream(Objects.requireNonNull(stalkerNotifications.getBody())).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId())
                        && n.getText().equals(NotificationMessages.Order.Suggested)
        ));
    }

    @Test
    void order_suggest_notFoundError() {
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
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

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

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

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

    @Test
    void order_getAvailable_stalker() {
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
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var stalkerAvailable = TestUtil.getAuthorized(restTemplate,
                "/api/orders/available",
                stalker,
                OrderResponse[].class);
        TestUtil.assertOK(stalkerAvailable);
        assertTrue(Objects.requireNonNull(stalkerAvailable.getBody()).length > 0);
        assertTrue(Arrays.stream(stalkerAvailable.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(result.getBody().getOrder().getId())));
    }

    @Test
    void order_accept_stalker_withNotifications() {
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
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var stalkerAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(stalkerAcceptResult);
        assertEquals(StatusIds.AcceptedByStalker, Objects.requireNonNull(stalkerAcceptResult.getBody()).getOrder().getStatusId());
        assertEquals(stalker.getUser().getId(), stalkerAcceptResult.getBody().getAssignedUser().getId());
        assertEquals(huckster.getUser().getId(), stalkerAcceptResult.getBody().getAcceptedUser().getId());
        assertNull(stalkerAcceptResult.getBody().getSuggestedUser());

        var hucksterNotifications = getNotifications(huckster);
        TestUtil.assertOK(hucksterNotifications);
        assertTrue(Arrays.stream(Objects.requireNonNull(hucksterNotifications.getBody())).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                        n.getText().equals(NotificationMessages.Order.AcceptedByStalker)));

        var clientNotifications = getNotifications(client);
        TestUtil.assertOK(clientNotifications);
        assertTrue(Arrays.stream(Objects.requireNonNull(clientNotifications.getBody())).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                n.getText().equals(NotificationMessages.Order.AcceptedByStalker)));

    }


    @Test
    void order_decline_stalker_withNotifications() {
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
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var stalkerDeclineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(stalkerDeclineResult);
        assertEquals(StatusIds.AcceptedByHuckster, Objects.requireNonNull(stalkerDeclineResult.getBody()).getOrder().getStatusId());
        assertEquals(huckster.getUser().getId(), stalkerDeclineResult.getBody().getAcceptedUser().getId());
        assertNull(stalkerDeclineResult.getBody().getAssignedUser());
        assertNull(stalkerDeclineResult.getBody().getSuggestedUser());

        var hucksterNotifications = getNotifications(huckster);
        TestUtil.assertOK(hucksterNotifications);
        assertTrue(Arrays.stream(Objects.requireNonNull(hucksterNotifications.getBody())).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                        n.getText().equals(NotificationMessages.Order.DeclinedByStalker)));
    }

    @Test
    void order_getAll_stalker() {
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
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var stalkerAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(stalkerAcceptResult);

        var stalkerGetAll = TestUtil.getAuthorized(restTemplate,
                "/api/orders",
                stalker,
                OrderResponse[].class);
        TestUtil.assertOK(stalkerGetAll);
        assertTrue(Arrays.stream(Objects.requireNonNull(stalkerGetAll.getBody())).anyMatch(o ->
                o.getOrder().getId().equals(result.getBody().getOrder().getId())));
    }


    @Test
    void order_start_stalker() {
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
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var stalkerAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(stalkerAcceptResult);

        var orderStartResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/start/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(orderStartResult);
        assertEquals(StatusIds.InProgress, Objects.requireNonNull(orderStartResult.getBody()).getOrder().getStatusId());
    }

    @Test
    void order_start_notFoundError() {
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);

        var orderStartResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/start/123123" ,
                stalker,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, orderStartResult.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, orderStartResult.getBody());
    }

    @Test
    void order_start_wrongUserError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);
        TestUtil.assertOK(result);

        var orderStartResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/start/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                client,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, orderStartResult.getStatusCode());
        assertEquals(ApiErrors.Order.CantStart, orderStartResult.getBody());
    }

    @Test
    void order_complete_stalker_withNotifications() {
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
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var stalkerAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(stalkerAcceptResult);

        var orderStartResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/start/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(orderStartResult);

        var orderCompleteResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/complete/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(orderCompleteResult);
        assertEquals(StatusIds.TransferredToHuckster, Objects.requireNonNull(orderCompleteResult.getBody()).getOrder().getStatusId());

        var clientNotifications = getNotifications(client);
        assertTrue(Arrays.stream(clientNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                n.getText().equals(NotificationMessages.Order.CompletedByStalker)));
    }


    @Test
    void order_complete_notFoundError() {
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);

        var orderCompleteResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/complete/123221" ,
                stalker,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, orderCompleteResult.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, orderCompleteResult.getBody());
    }

    @Test
    void order_complete_wrongRoleError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);
        TestUtil.assertOK(result);

        var orderStartResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/complete/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                client,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, orderStartResult.getStatusCode());
        assertEquals(ApiErrors.Order.CantComplete, orderStartResult.getBody());
    }

    @Test
    void order_suggest_toCourier_withoutStalker_withNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestToCourier= getSuggestRequest(courier.getUser(), result.getBody().getOrder());
        var suggestToCourierResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestToCourier,
                OrderResponse.class);
        TestUtil.assertOK(suggestToCourierResult);
        assertNotNull(Objects.requireNonNull(suggestToCourierResult.getBody()).getSuggestedUser());
        assertEquals(courier.getUser().getId(), suggestToCourierResult.getBody().getSuggestedUser().getId());
        assertEquals(StatusIds.AcceptedByHuckster, suggestToCourierResult.getBody().getOrder().getStatusId());
        assertNull(suggestToCourierResult.getBody().getAssignedUser());
        assertNull(suggestToCourierResult.getBody().getAcceptedCourier());

        var courierNotifications = getNotifications(courier);
        TestUtil.assertOK(courierNotifications);
        assertTrue(Arrays.stream(Objects.requireNonNull(courierNotifications.getBody())).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId())
                        && n.getText().equals(NotificationMessages.Order.Suggested)
        ));
    }

    @Test
    void order_suggest_toCourier_withStalker_withNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);
        var stalker = TestUtil.registerRole(restTemplate, Role.Stalker);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);

        TestUtil.assertOK(result);

        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);

        var suggestRequest= getSuggestRequest(stalker.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var stalkerAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(stalkerAcceptResult);

        var orderStartResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/start/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(orderStartResult);

        var orderCompleteResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/complete/" + result.getBody().getOrder().getId(),
                stalker,
                OrderResponse.class);
        TestUtil.assertOK(orderCompleteResult);

        var suggestToCourier= getSuggestRequest(courier.getUser(), result.getBody().getOrder());
        var suggestToCourierResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestToCourier,
                OrderResponse.class);
        TestUtil.assertOK(suggestToCourierResult);
        assertNotNull(Objects.requireNonNull(suggestToCourierResult.getBody()).getSuggestedUser());
        assertEquals(courier.getUser().getId(), suggestToCourierResult.getBody().getSuggestedUser().getId());
        assertEquals(StatusIds.TransferredToHuckster, suggestToCourierResult.getBody().getOrder().getStatusId());
        assertNotNull(suggestToCourierResult.getBody().getAssignedUser());
        assertNull(suggestToCourierResult.getBody().getAcceptedCourier());

        var courierNotifications = getNotifications(courier);
        TestUtil.assertOK(courierNotifications);
        assertTrue(Arrays.stream(Objects.requireNonNull(courierNotifications.getBody())).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId())
                        && n.getText().equals(NotificationMessages.Order.Suggested)
        ));
    }

    @Test
    void order_getAvailable_courier() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);
        TestUtil.assertOK(result);
        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);
        var suggestRequest= getSuggestRequest(courier.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var courierAvailable = TestUtil.getAuthorized(restTemplate,
                "/api/orders/available",
                courier,
                OrderResponse[].class);
        TestUtil.assertOK(courierAvailable);
        assertTrue(Arrays.stream(courierAvailable.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(result.getBody().getOrder().getId())));

    }
    @Test
    void order_accept_courier_withNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);
        TestUtil.assertOK(result);
        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);
        var suggestRequest= getSuggestRequest(courier.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var courierAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                courier,
                OrderResponse.class);

        TestUtil.assertOK(courierAcceptResult);
        assertNull(courierAcceptResult.getBody().getSuggestedUser());
        assertNotNull(courierAcceptResult.getBody().getAcceptedCourier());
        assertEquals(courier.getUser().getId(), courierAcceptResult.getBody().getAcceptedCourier().getId());
        assertEquals(StatusIds.Sent, courierAcceptResult.getBody().getOrderStatus().getId());


        var hucksterNotifications = getNotifications(huckster);
        assertTrue(Arrays.stream(hucksterNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                        n.getText().equals(NotificationMessages.Order.TransferredToCourier)));

        var clientNotifications = getNotifications(client);
        assertTrue(Arrays.stream(clientNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                n.getText().equals(NotificationMessages.Order.TransferredToCourier)));

    }

    @Test
    void order_decline_courier_withNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);
        TestUtil.assertOK(result);
        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);
        var suggestRequest= getSuggestRequest(courier.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var courierDeclineResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/decline/" + result.getBody().getOrder().getId(),
                courier,
                OrderResponse.class);

        TestUtil.assertOK(courierDeclineResult);
        assertNull(courierDeclineResult.getBody().getSuggestedUser());
        assertNull(courierDeclineResult.getBody().getAcceptedCourier());
        assertEquals(StatusIds.TransferredToHuckster, courierDeclineResult.getBody().getOrderStatus().getId());

        var hucksterNotifications = getNotifications(huckster);
        assertTrue(Arrays.stream(hucksterNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                        n.getText().equals(NotificationMessages.Order.DeclinedByCourier)));
    }
    //Courier get all

    @Test
    void order_getAll_courier() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);
        TestUtil.assertOK(result);
        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);
        var suggestRequest= getSuggestRequest(courier.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var courierAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                courier,
                OrderResponse.class);

        TestUtil.assertOK(courierAcceptResult);

        var courierOrders = TestUtil.getAuthorized(restTemplate,
                "/api/orders",
                courier,
                OrderResponse[].class);

        TestUtil.assertOK(courierOrders);
        assertTrue(Arrays.stream(courierOrders.getBody()).anyMatch(o ->
                o.getOrder().getId().equals(result.getBody().getOrder().getId())));
    }

    //Courier deliver

    @Test
    void order_deliver_withNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);
        TestUtil.assertOK(result);
        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);
        var suggestRequest= getSuggestRequest(courier.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var courierAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                courier,
                OrderResponse.class);

        TestUtil.assertOK(courierAcceptResult);

        var deliverResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/deliver/" + result.getBody().getOrder().getId(),
                courier,
                OrderResponse.class);
        TestUtil.assertOK(deliverResult);
        assertEquals(StatusIds.Delivered, deliverResult.getBody().getOrderStatus().getId());

        var hucksterNotifications = getNotifications(huckster);
        assertTrue(Arrays.stream(hucksterNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                        n.getText().equals(NotificationMessages.Order.WasDelivered)));

        var clientNotifications = getNotifications(client);
        assertTrue(Arrays.stream(clientNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                        n.getText().equals(NotificationMessages.Order.WasDelivered)));

    }


    @Test
    void order_deliver_wrongRoleError() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders/deliver/123213",
                client,
                ErrorResponse.class);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertEquals(ApiErrors.Order.CantDeliver, result.getBody());
    }

    @Test
    void order_deliver_notFoundError() {
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);

        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders/deliver/123213",
                courier,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals(ApiErrors.Order.NotFound, result.getBody());
    }

    //Client complete
    @Test
    void order_complete_client_withNotifications() {
        var client = TestUtil.registerRole(restTemplate, Role.Client);
        var courier = TestUtil.registerRole(restTemplate, Role.Courier);
        var huckster = TestUtil.registerRole(restTemplate, Role.Huckster);

        var createRequest = getCreateOrderRequest();
        var result = TestUtil.postAuthorized(restTemplate,
                "/api/orders",
                client,
                createRequest,
                OrderResponse.class);
        TestUtil.assertOK(result);
        var acceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + Objects.requireNonNull(result.getBody()).getOrder().getId(),
                huckster,
                OrderResponse.class);
        TestUtil.assertOK(acceptResult);
        var suggestRequest= getSuggestRequest(courier.getUser(), result.getBody().getOrder());
        var suggestResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/suggest",
                huckster,
                suggestRequest,
                OrderResponse.class);
        TestUtil.assertOK(suggestResult);

        var courierAcceptResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/accept/" + result.getBody().getOrder().getId(),
                courier,
                OrderResponse.class);

        TestUtil.assertOK(courierAcceptResult);

        var deliverResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/deliver/" + result.getBody().getOrder().getId(),
                courier,
                OrderResponse.class);
        TestUtil.assertOK(deliverResult);

        var clientCompleteResult = TestUtil.postAuthorized(restTemplate,
                "/api/orders/complete/" + result.getBody().getOrder().getId(),
                client,
                OrderResponse.class);
        TestUtil.assertOK(clientCompleteResult);
        assertEquals(StatusIds.Completed, clientCompleteResult.getBody().getOrderStatus().getId());

        var hucksterNotifications = getNotifications(huckster);
        assertTrue(Arrays.stream(hucksterNotifications.getBody()).anyMatch(n ->
                n.getOrderId().equals(result.getBody().getOrder().getId()) &&
                        n.getText().equals(NotificationMessages.Order.CompletedByClient)));
    }

    private ResponseEntity<NotificationResponse[]> getNotifications(AuthResponse user) {
        return TestUtil.getAuthorized(restTemplate,
                "/api/notifications",
                user,
                NotificationResponse[].class);
    }




    private CreateOrderRequest getCreateOrderRequest() {

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
