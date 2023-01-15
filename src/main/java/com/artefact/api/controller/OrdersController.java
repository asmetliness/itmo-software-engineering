package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.*;
import com.artefact.api.repository.*;
import com.artefact.api.repository.results.IOrderResult;
import com.artefact.api.request.CreateOrderRequest;
import com.artefact.api.request.SuggestOrderRequest;
import com.artefact.api.response.OrderResponse;
import com.artefact.api.utils.ApiErrors;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.NotificationMessages;
import com.artefact.api.utils.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final NotificationRepository notificationRepository;

    public OrdersController(OrderRepository orderRepository,
                            UserRepository userRepository,
                            NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }





    @PostMapping
    public ResponseEntity<Object> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();
        if(!user.getRole().equals(Role.Client)) {
            return new ResponseEntity<>(ApiErrors.Order.CantCreate, HttpStatus.FORBIDDEN);
        }

        var order = new Order();
        order.setArtifactId(request.getArtifactId());
        order.setCreatedUserId(userId);
        order.setStatusId(StatusIds.New);
        order.setPrice(request.getPrice());
        order.setDeliveryAddress(request.getDeliveryAddress());
        orderRepository.save(order);

        var hucksters = userRepository.findByRole(Role.Huckster);

        var notifications = Streams.from(hucksters)
                .map(u -> new Notification(NotificationMessages.Order.Created, u.getId(), order.getId()))
                .toList();
        notificationRepository.saveAll(notifications);

        return getOrderResponse(order.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrderResponse(@PathVariable("id") Long id) {

        var order = orderRepository.findByOrderId(id);
        if (order.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Order.NotFound, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new OrderResponse(order.get()), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Iterable<OrderResponse>> getOrderList() {
        var userId = Auth.userId();

        var user = userRepository.findById(userId);
        var role = user.get().getRole();

        var orders = switch (role) {
            case Client -> orderRepository.findByCreatedUserId(userId); //TESTED
            case Stalker -> orderRepository.findByAssignedUserId(userId);
            case Huckster -> orderRepository.findByAcceptedUserId(userId);
            default -> new ArrayList<IOrderResult>(); // TESTED
        };

        var response = Streams.from(orders).map(OrderResponse::new).toList();


        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/available")
    public ResponseEntity<Iterable<OrderResponse>> getAvailableOrders() {
        var userId = Auth.userId();

        var user = userRepository.findById(userId);

        var orders = switch (user.get().getRole()) {
            case Huckster -> orderRepository.findOrderByStatus(StatusIds.New); // TESTED

            case Stalker, Courier -> orderRepository.findSuggestedOrders(userId);
            default -> new ArrayList<IOrderResult>(); //TESTED
        };
        var response = Streams.from(orders)
                .map(OrderResponse::new).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<Object> acceptOrder(@PathVariable("id") long id) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();
        var role = user.getRole();

        var orderOpt = orderRepository.findById(id);

        //TESTED
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Order.NotFound, HttpStatus.NOT_FOUND);
        }
        Order order = orderOpt.get();

        if(!canAcceptOrder(user, order)) {
            return new ResponseEntity<>(ApiErrors.Order.CantAccept, HttpStatus.FORBIDDEN);
        }

        //TESTED
        if (role.equals(Role.Huckster)) {

            order.setAcceptedUserId(user.getId());
            order.setStatusId(StatusIds.AcceptedByHuckster);

            //TESTED
            notificationRepository.save(new Notification(NotificationMessages.Order.AcceptedByHuckster,
                    order.getCreatedUserId(),
                    order.getId()));
        }

        if (role.equals(Role.Stalker)) {
            order.setAssignedUserId(user.getId());
            order.setSuggestedUserId(null);
            order.setStatusId(StatusIds.AcceptedByStalker);

            notificationRepository.save(new Notification(NotificationMessages.Order.AcceptedByStalker,
                    order.getAcceptedUserId(),
                    order.getId()));

            notificationRepository.save(new Notification(NotificationMessages.Order.AcceptedByStalker,
                    order.getCreatedUserId(),
                    order.getId()));
        }

        if(role.equals(Role.Courier)) {
            order.setAcceptedCourierId(user.getId());
            order.setSuggestedUserId(null);
            order.setStatusId(StatusIds.Sent);

            notificationRepository.save(new Notification(NotificationMessages.Order.TransferredToCourier,
                    order.getAcceptedUserId(),
                    order.getId()));

            notificationRepository.save(new Notification(NotificationMessages.Order.TransferredToCourier,
                    order.getCreatedUserId(),
                    order.getId()));
        }

        orderRepository.save(order);
        return getOrderResponse(id);
    }

    private Boolean canAcceptOrder(User user, Order order) {
        if(user.getRole().equals(Role.Huckster)) {
            return order.getStatusId().equals(StatusIds.New);
        }
        if(user.getRole().equals(Role.Stalker)) {
            return order.getStatusId().equals(StatusIds.AcceptedByHuckster)
                    && order.getSuggestedUserId().equals(user.getId());
        }
        if(user.getRole().equals(Role.Courier)) {
            return order.getStatusId().equals(StatusIds.TransferredToHuckster)
                    && order.getSuggestedUserId().equals(user.getId());
        }
        return false;
    }

    @PostMapping("/decline/{id}")
    public ResponseEntity<Object> declineOrder(@PathVariable("id") long id) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();
        var role = user.getRole();

        var orderOpt = orderRepository.findById(id);

        //TESTED
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Order.NotFound, HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();

        if(!canDeclineOrder(user, order)) {
            return new ResponseEntity<>(ApiErrors.Order.CantDecline, HttpStatus.FORBIDDEN);
        }

        //TESTED
        if (role.equals(Role.Huckster)) {
            order.setAcceptedUserId(null);
            order.setStatusId(StatusIds.New);

            //TESTED
            notificationRepository.save(new Notification(NotificationMessages.Order.DeclinedByHuckster,
                    order.getCreatedUserId(),
                    order.getId()));
        }

        if (role.equals(Role.Stalker)) {
            order.setAssignedUserId(null);
            order.setSuggestedUserId(null);
            order.setStatusId(StatusIds.AcceptedByHuckster);

            notificationRepository.save(new Notification(NotificationMessages.Order.DeclinedByStalker,
                    order.getAcceptedUserId(),
                    order.getId()));
        }

        if(role.equals(Role.Courier)) {
            order.setSuggestedUserId(null);
            order.setAcceptedCourierId(null);
            order.setStatusId(StatusIds.TransferredToHuckster);

            notificationRepository.save(new Notification(NotificationMessages.Order.DeclinedByCourier,
                    order.getAcceptedUserId(),
                    order.getId()));
        }

        orderRepository.save(order);
        return getOrderResponse(id);
    }

    private Boolean canDeclineOrder(User user, Order order) {
        if(user.getRole().equals(Role.Huckster)) {
            return order.getStatusId().equals(StatusIds.New) ||
                    (order.getStatusId().equals(StatusIds.AcceptedByHuckster)
                            && order.getAcceptedUserId().equals(user.getId()));
        }

        if(user.getRole().equals(Role.Stalker)) {
            return order.getStatusId().equals(StatusIds.AcceptedByHuckster) ||
                    (order.getStatusId().equals(StatusIds.AcceptedByStalker)
                            && order.getAssignedUserId().equals(user.getId()));
        }

        if(user.getRole().equals(Role.Courier)) {
            return order.getStatusId().equals(StatusIds.TransferredToHuckster) ||
                    (order.getStatusId().equals(StatusIds.Sent) &&
                            order.getAcceptedCourierId().equals(user.getId()));
        }
        return false;
    }

    @PostMapping("/suggest")
    public ResponseEntity<Object> suggestOrder(@Valid  @RequestBody SuggestOrderRequest request) {

        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        var orderOpt = orderRepository.findById(request.getOrderId());
        if(orderOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Order.NotFound, HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();

        if(!user.getRole().equals(Role.Huckster) || !order.getAcceptedUserId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Order.AccessError, HttpStatus.FORBIDDEN);
        }
        var suggestedUser = userRepository.findById(request.getUserId());

        if(!canSuggestToUser(suggestedUser, order)) {
            return new ResponseEntity<>(ApiErrors.Order.CantSuggestToUser, HttpStatus.FORBIDDEN);
        }

        order.setSuggestedUserId(request.getUserId());
        orderRepository.save(order);

        notificationRepository.save(new Notification(NotificationMessages.Order.Suggested,
                request.getUserId(),
                request.getOrderId()));

        return getOrderResponse(request.getOrderId());
    }

    private Boolean canSuggestToUser(Optional<User> user, Order order) {
        if(user.isEmpty()) {
            return false;
        }
        if(user.get().getRole().equals(Role.Courier)) {
            return order.getStatusId().equals(StatusIds.TransferredToHuckster)
                    || order.getStatusId().equals(StatusIds.AcceptedByHuckster);
        }
        if(user.get().getRole().equals(Role.Stalker)) {
            return order.getStatusId().equals(StatusIds.AcceptedByHuckster);
        }
        return false;
    }

    @PostMapping("/start/{id}")
    public ResponseEntity<Object> startProgress(@PathVariable("id") long id) {

        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        if(!user.getRole().equals(Role.Stalker)) {
            return new ResponseEntity<>(ApiErrors.Order.CantStart, HttpStatus.FORBIDDEN);
        }
        var orderOpt = orderRepository.findById(id);
        if(orderOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Order.NotFound, HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();

        if(!order.getAssignedUserId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Order.CantStart, HttpStatus.FORBIDDEN);
        }

        order.setStatusId(StatusIds.InProgress);
        orderRepository.save(order);

        return getOrderResponse(order.getId());
    }

    @PostMapping("/complete/{id}")
    public ResponseEntity<Object> completeOrder(@PathVariable("id") long id) {

        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        var orderOpt = orderRepository.findById(id);
        if(orderOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Order.NotFound, HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();

        if(!canCompleteOrder(user, order)) {
            return new ResponseEntity<>(ApiErrors.Order.CantComplete, HttpStatus.NOT_FOUND);
        }

        if(user.getRole().equals(Role.Stalker)) {
            order.setStatusId(StatusIds.TransferredToHuckster);
        }
        if(user.getRole().equals(Role.Client)) {
            order.setStatusId(StatusIds.Completed);
        }
        orderRepository.save(order);

        return getOrderResponse(order.getId());
    }

    private Boolean canCompleteOrder(User user, Order order) {
        if(user.getRole().equals(Role.Stalker)) {
            return order.getStatusId().equals(StatusIds.InProgress) &&
                    order.getAcceptedUserId().equals(user.getId());
        }

        if(user.getRole().equals(Role.Client)) {
            return order.getStatusId().equals(StatusIds.Delivered) &&
                    order.getCreatedUserId().equals(user.getId());
        }

        return false;
    }

    @PostMapping("/deliver/{id}")
    public ResponseEntity<Object> deliverOrder(@PathVariable long id) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();
        if(!user.getRole().equals(Role.Courier)) {
            return new ResponseEntity<>(ApiErrors.Order.CantDeliver, HttpStatus.FORBIDDEN);
        }
        var orderOpt = orderRepository.findById(id);
        if(orderOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Order.NotFound, HttpStatus.NOT_FOUND);
        }
        var order = orderOpt.get();
        if(!order.getAcceptedCourierId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Order.CantDeliver, HttpStatus.FORBIDDEN);
        }

        order.setStatusId(StatusIds.Delivered);
        orderRepository.save(order);
        return getOrderResponse(id);
    }
}
