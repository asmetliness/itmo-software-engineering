package com.artefact.api.controller;

import com.artefact.api.consts.OrderStatusIds;
import com.artefact.api.consts.Role;
import com.artefact.api.model.*;
import com.artefact.api.repository.*;
import com.artefact.api.repository.results.IOrderResult;
import com.artefact.api.request.CreateOrderRequest;
import com.artefact.api.request.SuggestOrderRequest;
import com.artefact.api.response.OrderResponse;
import com.artefact.api.util.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    public ResponseEntity<Object> SuggestOrder(@RequestBody SuggestOrderRequest request) {
        var userId = (String) getContext().getAuthentication().getPrincipal();

        var order = orderRepository.findById(request.getOrderId());

        var orderVal = order.get();
        orderVal.setSuggestedUserId(request.getStalkerId());
        orderRepository.save(orderVal);

        notificationRepository.save(new Notification("Вам был предложен заказ!",
                request.getStalkerId(),
                request.getOrderId()));

        return GetOrderResponse(request.getOrderId());
    }

    @PostMapping("/decline/{id}")
    public ResponseEntity<Object> DeclineOrder(@PathVariable("id") long id) {
        var userId = (String) getContext().getAuthentication().getPrincipal();

        var user = userRepository.findById(Long.parseLong(userId));
        var role = user.get().getRole();

        var order = orderRepository.findById(id);

        if (!order.isPresent()) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }
        var orderVal = order.get();
        if (role.equals(Role.Huckster)) {
            orderVal.setAcceptedUserId(null);
            orderVal.setStatusId(OrderStatusIds.NewOrder);

            notificationRepository.save(new Notification("Ваш заказ был отклонен барыгой!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }
        if (role.equals(Role.Stalker)) {
            orderVal.setAssignedUserId(null);
            orderVal.setSuggestedUserId(null);
            orderVal.setStatusId(OrderStatusIds.AcceptedByHuckster);

            notificationRepository.save(new Notification("Заказ был отклонен сталкером!",
                    orderVal.getAcceptedUserId(),
                    orderVal.getId()));
            
            notificationRepository.save(new Notification("Заказ был отклонен сталкером!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }
        orderRepository.save(orderVal);
        return GetOrderResponse(id);
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<Object> AcceptOrder(@PathVariable("id") long id) {
        var userId = (String) getContext().getAuthentication().getPrincipal();

        var user = userRepository.findById(Long.parseLong(userId));
        var role = user.get().getRole();

        var order = orderRepository.findById(id);

        if (!order.isPresent()) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }
        Order orderVal = order.get();

        if (role.equals(Role.Huckster)) {
            orderVal.setAcceptedUserId(user.get().getId());
            orderVal.setStatusId(OrderStatusIds.AcceptedByHuckster);

            notificationRepository.save(new Notification("Заказ был принят барыгой!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }

        if (role.equals(Role.Stalker)) {
            orderVal.setAssignedUserId(user.get().getId());
            orderVal.setSuggestedUserId(null);
            orderVal.setStatusId(OrderStatusIds.AcceptedByStalker);

            notificationRepository.save(new Notification("Заказ был принят сталкером!",
                    orderVal.getAcceptedUserId(),
                    orderVal.getId()));

            notificationRepository.save(new Notification("Заказ был принят сталкером!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }
        orderRepository.save(orderVal);
        return GetOrderResponse(id);
    }

    @PostMapping
    public ResponseEntity<Object> CreateOrder(@RequestBody CreateOrderRequest request) {
        var userId = (String) getContext().getAuthentication().getPrincipal();

        var order = new Order();
        order.setArtifactId(request.getArtifactId());
        order.setCreatedUserId(Long.parseLong(userId));
        order.setStatusId(OrderStatusIds.NewOrder);
        order.setPrice(request.getPrice());

        orderRepository.save(order);

        var hucksters = userRepository.findByRole(Role.Huckster);

        var notifications = Streams.from(hucksters)
                .map(user -> new Notification("Был создан заказ", user.getId(), order.getId()))
                .toList();
        notificationRepository.saveAll(notifications);

        return GetOrderResponse(order.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> GetOrderResponse(@PathVariable("id") Long id) {

        var response = GetOrder(id);
        if (response == null) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Iterable<OrderResponse>> GetOrderList() {
        var userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var user = userRepository.findById(Long.parseLong(userId));
        var role = user.get().getRole();

        var orders = switch (role) {
            case Client -> orderRepository.findByCreatedUserId(Long.parseLong(userId));
            case Stalker -> orderRepository.findByAssignedUserId(Long.parseLong(userId));
            case Huckster -> orderRepository.findByAcceptedUserId(Long.parseLong(userId));
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
    public ResponseEntity<Iterable<OrderResponse>> GetAvailableOrders() {
        String userId = (String) getContext().getAuthentication().getPrincipal();

        var user = userRepository.findById(Long.parseLong(userId));
        var role = user.get().getRole();

        var orders = switch (role) {
            case Huckster -> orderRepository.findOrderByStatus(OrderStatusIds.NewOrder);
            case Stalker -> orderRepository.findSuggestedOrders(Long.parseLong(userId));
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
