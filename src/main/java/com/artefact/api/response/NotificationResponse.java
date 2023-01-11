package com.artefact.api.response;

import com.artefact.api.model.Notification;
import lombok.Data;

@Data
public class NotificationResponse {
    private boolean wasRead;
    private String text;
    private Long orderId;
    private Long weaponOrderId;
    private Long informationOrderId;

    public NotificationResponse() {}
    public NotificationResponse(Notification notification) {
        this.wasRead = notification.isWasRead();
        this.orderId = notification.getOrderId();
        this.weaponOrderId = notification.getWeaponOrderId();
        this.informationOrderId = notification.getInformationOrderId();
        this.text = notification.getMessage();
    }

}
