package com.artefact.api.response;

import com.artefact.api.model.*;
import com.artefact.api.repository.results.IOrderResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Order order;
    private User createdUser;
    private User acceptedUser;
    private User suggestedUser;
    private User assignedUser;
    private User acceptedCourier;

    private Status orderStatus;
    private Artifact artifact;


    public OrderResponse(IOrderResult orderResult) {
        order = orderResult.getOrder();
        createdUser = orderResult.getCreatedUser();
        acceptedUser = orderResult.getAcceptedUser();
        assignedUser = orderResult.getAssignedUser();
        orderStatus = orderResult.getStatus();
        artifact = orderResult.getArtifact();
        acceptedCourier = orderResult.getAcceptedCourier();
        suggestedUser = orderResult.getSuggestedUser();
    }

    public OrderResponse(Order order) {
        this.order = order;
        createdUser = order.getCreatedUser();
        acceptedUser = order.getAcceptedUser();
        assignedUser = order.getAssignedUser();
        orderStatus = order.getStatus();
        artifact = order.getArtifact();
        acceptedCourier = order.getAcceptedCourier();
        suggestedUser = order.getSuggestedUser();
    }
}
