package com.artefact.api.response;

import com.artefact.api.model.Information;
import com.artefact.api.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InformationResponse {
    private Information information;
    private User createdUser;
    private User acceptedUser;

}
