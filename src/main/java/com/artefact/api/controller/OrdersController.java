package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.*;
import com.artefact.api.repository.*;
import com.artefact.api.repository.results.IOrderResult;
import com.artefact.api.request.CreateOrderRequest;
import com.artefact.api.request.SuggestOrderRequest;
import com.artefact.api.response.OrderResponse;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

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


    @PostMapping("/suggest")
    public ResponseEntity<Object> suggestOrder(@RequestBody SuggestOrderRequest request) {

        var order = orderRepository.findById(request.getOrderId());

        var orderVal = order.get();
        orderVal.setSuggestedUserId(request.getUserId());
        orderRepository.save(orderVal);

        notificationRepository.save(new Notification("Вам был предложен заказ!",
                request.getUserId(),
                request.getOrderId()));

        return getOrderResponse(request.getOrderId());
    }

    @PostMapping("/decline/{id}")
    public ResponseEntity<Object> declineOrder(@PathVariable("id") long id) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId);
        var role = user.get().getRole();

        var order = orderRepository.findById(id);

        if (order.isEmpty()) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }
        var orderVal = order.get();
        if (role.equals(Role.Huckster)) {
            orderVal.setAcceptedUserId(null);
            orderVal.setStatusId(StatusIds.New);

            notificationRepository.save(new Notification("Ваш заказ был отклонен барыгой!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }
        if (role.equals(Role.Stalker)) {
            orderVal.setAssignedUserId(null);
            orderVal.setSuggestedUserId(null);
            orderVal.setStatusId(StatusIds.AcceptedByHuckster);

            notificationRepository.save(new Notification("Заказ был отклонен сталкером!",
                    orderVal.getAcceptedUserId(),
                    orderVal.getId()));
        }

        if(role.equals(Role.Courier)) {
            orderVal.setSuggestedUserId(null);
            orderVal.setAcceptedCourierId(null);

            notificationRepository.save(new Notification("Заказ был отклонен курьером!",
                    orderVal.getAcceptedUserId(),
                    orderVal.getId()));
        }

        orderRepository.save(orderVal);
        return getOrderResponse(id);
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<Object> acceptOrder(@PathVariable("id") long id) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId);
        var role = user.get().getRole();

        var order = orderRepository.findById(id);

        if (order.isEmpty()) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }
        Order orderVal = order.get();

        if (role.equals(Role.Huckster)) {
            orderVal.setAcceptedUserId(user.get().getId());
            orderVal.setStatusId(StatusIds.AcceptedByHuckster);

            notificationRepository.save(new Notification("Заказ был принят барыгой!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }

        if (role.equals(Role.Stalker)) {
            orderVal.setAssignedUserId(user.get().getId());
            orderVal.setSuggestedUserId(null);
            orderVal.setStatusId(StatusIds.AcceptedByStalker);

            notificationRepository.save(new Notification("Заказ был принят сталкером!",
                    orderVal.getAcceptedUserId(),
                    orderVal.getId()));

            notificationRepository.save(new Notification("Заказ был принят сталкером!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }

        if(role.equals(Role.Courier)) {
            orderVal.setAcceptedCourierId(user.get().getId());
            orderVal.setSuggestedUserId(null);
            orderVal.setStatusId(StatusIds.Sent);

            notificationRepository.save(new Notification("Заказ был передан курьеру!",
                    orderVal.getAcceptedUserId(),
                    orderVal.getId()));

            notificationRepository.save(new Notification("Заказ был передан курьеру!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }

        orderRepository.save(orderVal);
        return getOrderResponse(id);
    }

    @PostMapping
    public ResponseEntity<Object> createOrder(@RequestBody CreateOrderRequest request) {
        var userId = Auth.userId();

        var order = new Order();
        order.setArtifactId(request.getArtifactId());
        order.setCreatedUserId(userId);
        order.setStatusId(StatusIds.New);
        order.setPrice(request.getPrice());

        orderRepository.save(order);

        var hucksters = userRepository.findByRole(Role.Huckster);

        var notifications = Streams.from(hucksters)
                .map(user -> new Notification("Был создан заказ", user.getId(), order.getId()))
                .toList();
        notificationRepository.saveAll(notifications);

        return getOrderResponse(order.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrderResponse(@PathVariable("id") Long id) {

        var response = GetOrder(id);
        if (response == null) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Iterable<OrderResponse>> getOrderList() {
        var userId = Auth.userId();

        var user = userRepository.findById(userId);
        var role = user.get().getRole();

        var orders = switch (role) {
            case Client -> orderRepository.findByCreatedUserId(userId);
            case Stalker -> orderRepository.findByAssignedUserId(userId);
            case Huckster -> orderRepository.findByAcceptedUserId(userId);
            default -> new ArrayList<IOrderResult>();
        };

        var response = Streams.from(orders).map(order -> new OrderResponse(
                order.getOrder(),
                order.getCreatedUser(),
                order.getAcceptedUser(),
                order.getAssignedUser(),
                order.getStatus(),
                order.getArtifact()
        )).toList();


        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private OrderResponse GetOrder(Long id) {
        var order = orderRepository.findById(id);
        if (order.isEmpty()) {
            return null;
        }

        return new OrderResponse(
                order.get(),
                order.get().getCreatedUser(),
                order.get().getAcceptedUser(),
                order.get().getAssignedUser(),
                order.get().getStatus(),
                order.get().getArtifact());
    }

    @GetMapping("/available")
    public ResponseEntity<Iterable<OrderResponse>> getAvailableOrders() {
        var userId = Auth.userId();

        var user = userRepository.findById(userId);

        var orders = switch (user.get().getRole()) {
            case Huckster -> orderRepository.findOrderByStatus(StatusIds.New);
            case Stalker, Courier -> orderRepository.findSuggestedOrders(userId);
            default -> new ArrayList<IOrderResult>();
        };
        var response = Streams.from(orders)
                .map(order -> new OrderResponse(
                        order.getOrder(),
                        order.getCreatedUser(),
                        order.getAcceptedUser(),
                        order.getAssignedUser(),
                        order.getStatus(),
                        order.getArtifact())
                ).toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
