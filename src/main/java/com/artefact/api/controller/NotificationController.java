package com.artefact.api.controller;

import com.artefact.api.model.Notification;
import com.artefact.api.repository.NotificationRepository;
import com.artefact.api.response.NotificationResponse;
import com.artefact.api.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

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
        var userId = (String) getContext().getAuthentication().getPrincipal();

        var userNotifications = notificationRepository.findByUserId(Long.parseLong(userId));

        var result = Streams.from(userNotifications)
                .map(notification -> new NotificationResponse(notification.isWasRead(), notification.getMessage(), notification.getOrderId()))
                .toList();

        userNotifications.forEach(n -> n.setWasRead(true));

        notificationRepository.saveAll(userNotifications);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
