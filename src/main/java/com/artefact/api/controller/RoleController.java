package com.artefact.api.controller;


import com.artefact.api.model.Role;
import com.artefact.api.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;


    @GetMapping
    public ResponseEntity<Iterable<Role>> getAllRoles() {

        Iterable<Role> roles = roleRepository.findAll();

        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

}
