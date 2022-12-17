package com.artefact.api.controller;

import com.artefact.api.repository.NotificationRepository;
import com.artefact.api.response.NotificationResponse;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Controller
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("")
    public ResponseEntity<Iterable<NotificationResponse>> getNotifications() {
        var userId = Auth.userId();

        var userNotifications = notificationRepository.findByUserId(userId);

        var result = Streams.from(userNotifications)
                .map(notification -> new NotificationResponse(notification))
                .toList();

        userNotifications.forEach(n -> n.setWasRead(true));

        notificationRepository.saveAll(userNotifications);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
