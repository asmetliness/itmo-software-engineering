package com.artefact.api.response;

import com.artefact.api.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserResponse {
    private User user;
    private String role;
}
