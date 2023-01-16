package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.response.OrderResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = Role.class)))} ) })
    public ResponseEntity<Iterable<Role>> getAllRoles() {
        var stream = Arrays.stream(Role.values());
        return new ResponseEntity<>(stream.toList(), HttpStatus.OK);
    }
}
