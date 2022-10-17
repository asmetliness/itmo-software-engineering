package com.artefact.api.repository.results;

import com.artefact.api.model.Artifact;
import com.artefact.api.model.Order;
import com.artefact.api.model.OrderStatus;
import com.artefact.api.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;


public interface IOrderResult {

    Order getOrder();
    OrderStatus getStatus();
    Artifact getArtifact();
    User getCreatedUser();
    User getAcceptedUser();
    User getAssignedUser();
    User getSuggestedUser();
}
