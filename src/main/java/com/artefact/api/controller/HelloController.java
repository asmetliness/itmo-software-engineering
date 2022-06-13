package com.artefact.api.controller;

import com.artefact.api.model.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.RoleRepository;
import com.artefact.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/test")
    public String sayHello() {


        User test = userRepository.getByLogin("logi222n");
        return "Hello!";

    }
}
