package com.artefact.api.controller;

import com.artefact.api.model.Artifact;
import com.artefact.api.model.Notification;
import com.artefact.api.repository.ArtifactRepository;
import com.artefact.api.repository.NotificationRepository;
import com.artefact.api.response.NotificationResponse;
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
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("")
    public ResponseEntity<Iterable<NotificationResponse>> getNotifications() {
        String userId = (String) getContext().getAuthentication().getPrincipal();

        Iterable<Notification> userNotifications = notificationRepository.findByUserId(Long.parseLong(userId));

        ArrayList<NotificationResponse> result = new ArrayList<>();

        for(Notification notification: userNotifications) {
            result.add(new NotificationResponse(notification.isWasRead(), notification.getMessage(), notification.getOrderId()));
            notification.setWasRead(true);
        }
        notificationRepository.saveAll(userNotifications);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
