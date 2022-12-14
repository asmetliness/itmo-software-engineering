package com.artefact.api.repository.results;

import com.artefact.api.model.Artifact;
import com.artefact.api.model.Order;
import com.artefact.api.model.Status;
import com.artefact.api.model.User;


public interface IOrderResult {

    Order getOrder();
    Status getStatus();
    Artifact getArtifact();
    User getCreatedUser();
    User getAcceptedUser();
    User getAssignedUser();
    User getSuggestedUser();
}
