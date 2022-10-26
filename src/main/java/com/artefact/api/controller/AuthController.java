package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.LoginRequest;
import com.artefact.api.request.RegisterRequest;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Object> Register(@RequestBody RegisterRequest request) {

        var existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return new ResponseEntity<>("Пользователь с таким email уже существует", HttpStatus.BAD_REQUEST);
        }

        var role = request.getRole();
        if (role == null) {
            return new ResponseEntity<>("Переданная роль не найдена", HttpStatus.BAD_REQUEST);
        }

        if (!Role.isPresent(role)) {
            return new ResponseEntity<>("Переданная роль не найдена", HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<Object> Login(@RequestBody LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (!user.isPresent()) {
            return new ResponseEntity<>("Пользователь не найден!", HttpStatus.NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
            return new ResponseEntity<>("Пользователь не найден!", HttpStatus.NOT_FOUND);
        }
        String token = jwtUtil.generateToken(user.get().getId());
        return new ResponseEntity<>(new AuthResponse(token, user.get()), HttpStatus.OK);
    }
}