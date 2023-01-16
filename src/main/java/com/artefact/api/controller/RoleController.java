package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @GetMapping
    public ResponseEntity<Iterable<Role>> getAllRoles() {
        var stream = Arrays.stream(Role.values());
        return new ResponseEntity<>(stream.toList(), HttpStatus.OK);
    }
}
