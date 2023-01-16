package com.artefact.api.controller;

import com.artefact.api.model.Artifact;
import com.artefact.api.repository.ArtifactRepository;
import com.artefact.api.response.WeaponResponse;
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

@RestController
@RequestMapping("/api/artifacts")
public class ArtifactController {
    private final ArtifactRepository artifactRepository;

    public ArtifactController(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    @GetMapping("")
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = Artifact.class)))} ) })
    public ResponseEntity<Iterable<Artifact>> getArtifacts() {
        return new ResponseEntity<>(artifactRepository.findAll(), HttpStatus.OK);
    }
}
