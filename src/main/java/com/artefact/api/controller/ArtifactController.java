package com.artefact.api.controller;

import com.artefact.api.model.Artifact;
import com.artefact.api.repository.ArtifactRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/artifacts")
public class ArtifactController {
    private final ArtifactRepository artifactRepository;

    public ArtifactController(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    @GetMapping("")
    public ResponseEntity<Iterable<Artifact>> getArtifacts() {
        return new ResponseEntity<>(artifactRepository.findAll(), HttpStatus.OK);
    }
}
