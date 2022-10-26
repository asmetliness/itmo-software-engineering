package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

@Controller
@RequestMapping("/api/roles")
public class RoleController {
    public RoleController() {}

    @GetMapping
    public ResponseEntity<Iterable<String>> getAllRoles() {
        var roles = Arrays.asList(Role.ALL);
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }
}
