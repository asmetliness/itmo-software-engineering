package com.artefact.api.controller;

import com.artefact.api.consts.OrderStatusIds;
import com.artefact.api.consts.RoleNames;
import com.artefact.api.model.*;
import com.artefact.api.repository.*;
import com.artefact.api.request.CreateOrderRequest;
import com.artefact.api.request.SuggestOrderRequest;
import com.artefact.api.response.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Controller
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final OrderStatusRepository orderStatusRepository;

    private final RoleRepository roleRepository;

    private final ArtifactRepository artifactRepository;

    private final NotificationRepository notificationRepository;

    public OrdersController(OrderRepository orderRepository,
                            UserRepository userRepository,
                            OrderStatusRepository orderStatusRepository,
                            RoleRepository roleRepository,
                            ArtifactRepository artifactRepository,
                            NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.roleRepository = roleRepository;
        this.artifactRepository = artifactRepository;
        this.notificationRepository = notificationRepository;
    }


    @PostMapping("/suggest")
    public ResponseEntity<Object> SuggestOrder(@RequestBody SuggestOrderRequest request) {
        String userId = (String) getContext().getAuthentication().getPrincipal();

        Optional<Order> order = orderRepository.findById(request.getOrderId());

        Order orderVal = order.get();
        orderVal.setSuggestedUserId(request.getStalkerId());
        orderRepository.save(orderVal);

        notificationRepository.save(new Notification("Вам был предложен заказ!",
                request.getStalkerId(),
                request.getOrderId()));

        return GetOrderResponse(request.getOrderId());
    }

    @PostMapping("/decline/{id}")
    public ResponseEntity<Object> DeclineOrder(@PathVariable("id") long id) {
        String userId = (String) getContext().getAuthentication().getPrincipal();

        Optional<User> user = userRepository.findById(Long.parseLong(userId));
        Optional<Role> role = roleRepository.findById(user.get().getRoleId());

        Optional<Order> order = orderRepository.findById(id);

        if (!order.isPresent()) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }
        Order orderVal = order.get();
        if (role.get().getName().equals(RoleNames.Huckster)) {
            orderVal.setAcceptedUserId(null);
            orderVal.setStatusId(OrderStatusIds.NewOrder);

            notificationRepository.save(new Notification("Ваш заказ был отклонен барыгой!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }
        if (role.get().getName().equals(RoleNames.Stalker)) {
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
        String userId = (String) getContext().getAuthentication().getPrincipal();

        Optional<User> user = userRepository.findById(Long.parseLong(userId));
        Optional<Role> role = roleRepository.findById(user.get().getRoleId());

        Optional<Order> order = orderRepository.findById(id);

        if (!order.isPresent()) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }
        Order orderVal = order.get();
        if (role.get().getName().equals(RoleNames.Huckster)) {
            orderVal.setAcceptedUserId(user.get().getId());
            orderVal.setStatusId(OrderStatusIds.AcceptedByHuckster);

            notificationRepository.save(new Notification("Заказ был принят барыгой!",
                    orderVal.getCreatedUserId(),
                    orderVal.getId()));
        }
        if (role.get().getName().equals(RoleNames.Stalker)) {
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
        String userId = (String) getContext().getAuthentication().getPrincipal();

        Order order = new Order();
        order.setArtifactId(request.getArtifactId());
        order.setCreatedUserId(Long.parseLong(userId));
        order.setStatusId(OrderStatusIds.NewOrder);
        order.setPrice(request.getPrice());

        orderRepository.save(order);

        return GetOrderResponse(order.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> GetOrderResponse(@PathVariable("id") Long id) {

        OrderResponse response = GetOrder(id);
        if (response == null) {
            return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Iterable<OrderResponse>> GetOrderList() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<User> user = userRepository.findById(Long.parseLong(userId));
        Optional<Role> role = roleRepository.findById(user.get().getRoleId());

        Iterable<Order> orders = null;
        String name = role.get().getName();

        if (role.get().getName().equals(RoleNames.Client)) {
            orders = orderRepository.findByCreatedUserId(Long.parseLong(userId));
        }

        if (role.get().getName().equals(RoleNames.Stalker)) {
            orders = orderRepository.findByAssignedUserId(Long.parseLong(userId));
        }

        if (role.get().getName().equals(RoleNames.Huckster)) {
            orders = orderRepository.findByAcceptedUserId(Long.parseLong(userId));
        }

        if (orders == null) {
            orders = new ArrayList<>();
        }

        ArrayList<OrderResponse> response = new ArrayList<>();
        for (Order order : orders) {
            response.add(GetOrder(order.getId()));
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private OrderResponse GetOrder(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (!order.isPresent()) {
            return null;
        }

        User createdUser = null;
        if (order.get().getCreatedUserId() != null) {
            createdUser = userRepository.findById(order.get().getCreatedUserId()).orElse(null);
        }
        User acceptedUser = null;
        if (order.get().getAcceptedUserId() != null) {
            acceptedUser = userRepository.findById(order.get().getAcceptedUserId()).orElse(null);
        }
        User assignedUser = null;
        if (order.get().getAssignedUserId() != null) {
            assignedUser = userRepository.findById(order.get().getAssignedUserId()).orElse(null);
        }
        Optional<OrderStatus> status = orderStatusRepository.findById(order.get().getStatusId());

        Artifact artifact = artifactRepository.findById(order.get().getArtifactId()).get();

        OrderResponse response = new OrderResponse(
                order.get(),
                createdUser,
                acceptedUser,
                assignedUser,
                status.get(),
                artifact);
        return response;
    }

    @GetMapping("/available")
    public ResponseEntity<Iterable<OrderResponse>> GetAvailableOrders() {
        String userId = (String) getContext().getAuthentication().getPrincipal();

        Optional<User> user = userRepository.findById(Long.parseLong(userId));
        Optional<Role> role = roleRepository.findById(user.get().getRoleId());

        Iterable<Order> orders = null;
        if (Objects.equals(role.get().getName(), RoleNames.Huckster)) {
            orders = orderRepository.findOrderByStatus(OrderStatusIds.NewOrder);
        }
        if (Objects.equals(role.get().getName(), RoleNames.Stalker)) {
            orders = orderRepository.findSuggestedOrders(Long.parseLong(userId));
        }

        if (orders == null) {
            orders = new ArrayList<>();
        }

        ArrayList<OrderResponse> response = new ArrayList<>();
        for (Order order : orders) {
            response.add(GetOrder(order.getId()));
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
