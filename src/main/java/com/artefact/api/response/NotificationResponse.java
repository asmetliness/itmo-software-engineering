package com.artefact.api.response;

import com.artefact.api.model.Role;
import com.artefact.api.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private boolean wasRead;
    private String text;
    private long orderId;
}
