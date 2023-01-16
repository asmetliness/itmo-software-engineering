package com.artefact.api.controller;

import com.artefact.api.model.Artifact;
import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.LoginRequest;
import com.artefact.api.request.RegisterRequest;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.security.JwtUtil;
import com.artefact.api.utils.ApiErrors;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/register")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = AuthResponse.class))} ) })
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {

        var existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return new ResponseEntity<>(ApiErrors.Auth.UserAlreadyExists, HttpStatus.BAD_REQUEST);
        }

        var role = request.getRole();
        if (role == null) {
            return new ResponseEntity<>(ApiErrors.Auth.RoleNotFound, HttpStatus.BAD_REQUEST);
        }

        var user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());

        return new ResponseEntity<>(new AuthResponse(token, user), HttpStatus.OK);
    }

    @PostMapping("/login")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = AuthResponse.class))} ) })
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Auth.UserNotFound, HttpStatus.NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
            return new ResponseEntity<>(ApiErrors.Auth.UserNotFound, HttpStatus.NOT_FOUND);
        }
        String token = jwtUtil.generateToken(user.get().getId());
        return new ResponseEntity<>(new AuthResponse(token, user.get()), HttpStatus.OK);
    }
}