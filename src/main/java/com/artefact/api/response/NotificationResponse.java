package com.artefact.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private boolean wasRead;
    private String text;
    private long orderId;
}
