package com.artefact.api.response;

import com.artefact.api.model.Role;
import com.artefact.api.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private User user;
    private Role role;
}
