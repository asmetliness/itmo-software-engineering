package com.artefact.api.controller;

import com.artefact.api.consts.RoleNames;
import com.artefact.api.model.Order;
import com.artefact.api.model.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.OrderRepository;
import com.artefact.api.repository.RoleRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    OrderRepository orderRepository;

    @GetMapping("/current")
    public ResponseEntity<UserResponse> getUserDetails() {

        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(Long.parseLong(userId)).get();
        return new ResponseEntity<>(new UserResponse(user, user.getRole()), HttpStatus.OK);
    }

    @GetMapping("/stalkers")
    public ResponseEntity<Iterable<User>> getAllStalkers() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(Long.parseLong(userId)).get();
        Role role = user.getRole();

        if (!role.getName().equals(RoleNames.Huckster)) {
            return new ResponseEntity<>(new ArrayList<User>(), HttpStatus.OK);
        }

        Role stalkerRole = roleRepository.findByName(RoleNames.Stalker);

        Iterable<User> users = userRepository.findByRole(stalkerRole.getId());

        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
