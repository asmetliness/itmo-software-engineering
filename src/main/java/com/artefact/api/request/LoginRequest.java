package com.artefact.api.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String login;
    private String password;
}
