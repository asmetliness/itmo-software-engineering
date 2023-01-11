package com.artefact.api.response;

import com.artefact.api.model.Information;
import com.artefact.api.model.Status;
import com.artefact.api.model.User;
import com.artefact.api.repository.results.IInformationResult;
import lombok.Data;

@Data
public class InformationResponse {
    private Information information;
    private User createdUser;
    private User acquiredUser;
    private User requestedUser;

    private Status status;
    public InformationResponse(IInformationResult informationResult) {
        information = informationResult.getInformation();
        createdUser = informationResult.getCreatedUser();
        acquiredUser = informationResult.getAcquiredUser();
        requestedUser = informationResult.getRequestedUser();
        status = informationResult.getStatus();
    }

    public InformationResponse(){}

}
