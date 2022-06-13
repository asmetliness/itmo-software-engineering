package com.artefact.api.controller;

import com.artefact.api.repository.RoleRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.LoginRequest;
import com.artefact.api.response.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    @PostMapping("/api/auth")
    public AuthResponse Login(@RequestBody LoginRequest request) {

        return new AuthResponse();
    }
}
