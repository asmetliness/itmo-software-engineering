package com.artefact.api.controller;

import com.artefact.api.consts.OrderStatusIds;
import com.artefact.api.consts.RoleNames;
import com.artefact.api.model.*;
import com.artefact.api.repository.*;
import com.artefact.api.request.CreateOrderRequest;
import com.artefact.api.response.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Controller
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ArtifactRepository artifactRepository;

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
        if(response == null) {
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

        if(role.get().getName().equals(RoleNames.Client)) {
            orders = orderRepository.findByCreatedUserId(Long.parseLong(userId));
        }

        if(role.get().getName().equals(RoleNames.Stalker)) {
            orders = orderRepository.findByAssignedUserId(Long.parseLong(userId));
        }

        if(role.get().getName().equals(RoleNames.Huckster)) {
            orders = orderRepository.findByAcceptedUserId(Long.parseLong(userId));
        }

        if(orders == null) {
            orders = new ArrayList<>();
        }

        ArrayList<OrderResponse> response = new ArrayList<>();
        for(Order order: orders) {
            response.add(GetOrder(order.getId()));
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private OrderResponse GetOrder(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if(!order.isPresent()) {
            return null;
        }

        User createdUser = null;
        if(order.get().getCreatedUserId() != null) {
            createdUser = userRepository.findById(order.get().getCreatedUserId()).orElse(null);
        }
        User acceptedUser = null;
        if(order.get().getAcceptedUserId() != null) {
            acceptedUser = userRepository.findById(order.get().getAcceptedUserId()).orElse(null);
        }
        User assignedUser = null;
        if(order.get().getAssignedUserId() != null) {
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
        if(role.get().getName() == RoleNames.Huckster) {
            orders = orderRepository.findOrderByStatus(OrderStatusIds.NewOrder);
        }
        if(role.get().getName() == RoleNames.Stalker) {
            orders = orderRepository.findSuggestedOrders(Long.parseLong(userId));
        }

        if(orders == null) {
            orders = new ArrayList<>();
        }

        ArrayList<OrderResponse> response = new ArrayList<>();
        for(Order order: orders) {
            response.add(GetOrder(order.getId()));
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
