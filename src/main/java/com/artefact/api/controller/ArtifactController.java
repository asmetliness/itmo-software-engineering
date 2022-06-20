package com.artefact.api.controller;

import com.artefact.api.model.Artifact;
import com.artefact.api.model.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.ArtifactRepository;
import com.artefact.api.repository.RoleRepository;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/artifacts")
public class ArtifactController {
    @Autowired
    private ArtifactRepository artifactRepository;

    @GetMapping("")
    public ResponseEntity<Iterable<Artifact>> getArtifacts() {
        return new ResponseEntity<>(artifactRepository.findAll(), HttpStatus.OK);
    }
}
