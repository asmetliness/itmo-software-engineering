package com.artefact.api.repository.results;

import com.artefact.api.model.Information;
import com.artefact.api.model.User;

public interface IInformationResult {
    Information getInformation();
    User getCreatedUser();
    User getAcceptedUser();
}