package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

@Controller
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<UserResponse> getUserDetails() {

        var userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var user = userRepository.findById(Long.parseLong(userId)).get();
        return new ResponseEntity<>(new UserResponse(user), HttpStatus.OK);
    }

    @GetMapping("/stalkers")
    public ResponseEntity<Iterable<User>> getAllStalkers() {
        var userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var user = userRepository.findById(Long.parseLong(userId)).get();
        var role = user.getRole();

        if (!role.equals(Role.Huckster)) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        var users = userRepository.findByRole(Role.Stalker);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
