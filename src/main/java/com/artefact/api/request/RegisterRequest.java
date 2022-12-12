package com.artefact.api.request;

import com.artefact.api.consts.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class RegisterRequest {
    private String email;
    private String password;
    private Role role;
}
