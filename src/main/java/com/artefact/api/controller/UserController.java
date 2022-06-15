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
    public ResponseEntity<UserResponse> getUserDetails(){

        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(Long.parseLong(userId)).get();
        Role role = roleRepository.findById(user.getRoleId()).get();
        return new ResponseEntity<>(new UserResponse(user, role), HttpStatus.OK);
    }


}
