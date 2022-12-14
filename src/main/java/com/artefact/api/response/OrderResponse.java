package com.artefact.api.response;

import com.artefact.api.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Order order;
    private User createdUser;
    private User acceptedUser;
    private User assignedUser;
    private Status orderStatus;
    private Artifact artifact;
}
