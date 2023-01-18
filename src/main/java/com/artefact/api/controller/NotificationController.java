package com.artefact.api.controller;

import com.artefact.api.repository.NotificationRepository;
import com.artefact.api.response.InformationResponse;
import com.artefact.api.response.NewNotificationsResponse;
import com.artefact.api.response.NotificationResponse;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.Streams;
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
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("")
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = NotificationResponse.class)))} ) })
    public ResponseEntity<Iterable<NotificationResponse>> getNotifications() {
        var userId = Auth.userId();

        var userNotifications = notificationRepository.findByUserId(userId);

        var result = Streams.from(userNotifications)
                .map(NotificationResponse::new)
                .toList();

        userNotifications.forEach(n -> n.setWasRead(true));

        notificationRepository.saveAll(userNotifications);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/new")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = NewNotificationsResponse.class))} ) })
    public ResponseEntity<NewNotificationsResponse> checkNewNotifications() {
        var userId = Auth.userId();
        var userNotifications = notificationRepository.findByUserId(userId);

        var hasNew = Streams.from(userNotifications).anyMatch(n -> !n.isWasRead());

        return new ResponseEntity<>(new NewNotificationsResponse(hasNew), HttpStatus.OK);
    }
}
