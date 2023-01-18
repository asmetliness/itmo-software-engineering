package com.artefact.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewNotificationsResponse {

    private boolean hasNotifications;

    public NewNotificationsResponse() {}
}
