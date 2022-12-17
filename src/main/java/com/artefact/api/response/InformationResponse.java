package com.artefact.api.response;

import com.artefact.api.model.Information;
import com.artefact.api.model.User;
import com.artefact.api.repository.results.IInformationResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class InformationResponse {
    private Information information;
    private User createdUser;
    private User acquiredUser;
    private User requestedUser;

    public InformationResponse(IInformationResult informationResult) {
        information = informationResult.getInformation();
        createdUser = informationResult.getCreatedUser();
        acquiredUser = informationResult.getAcquiredUser();
        requestedUser = informationResult.getRequestedUser();
    }

}
