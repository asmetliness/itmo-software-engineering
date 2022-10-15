package com.artefact.api.controller;

import com.artefact.api.model.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.RoleRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.request.LoginRequest;
import com.artefact.api.request.RegisterRequest;
import com.artefact.api.response.AuthResponse;
import com.artefact.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;



    @PostMapping("/test")
    public ResponseEntity Test() {
        Optional<User> existingUser = userRepository.findByEmail("test");


        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> Register(@RequestBody RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser != null) {
            return new ResponseEntity<>("Пользователь с таким email уже существует", HttpStatus.BAD_REQUEST);
        }

        if (request.getRoleId() == null) {
            return new ResponseEntity<>("Переданная роль не найдена", HttpStatus.BAD_REQUEST);
        }

        Optional<Role> role = roleRepository.findById(request.getRoleId());
        if (!role.isPresent()) {
            return new ResponseEntity<>("Переданная роль не найдена", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoleId(request.getRoleId());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());


        Optional<User> test = userRepository.findById(user.getId());

        return new ResponseEntity<>(new AuthResponse(token, user, role.get()), HttpStatus.OK);
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
        return new ResponseEntity<>(new AuthResponse(token, user.get(), user.get().getRole()), HttpStatus.OK);
    }
}