package com.artefact.api.controller;

import com.artefact.api.model.User;
import com.artefact.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test222")
    public ResponseEntity<User> getUserDetails(){

        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(Long.parseLong(userId)).get();

        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
