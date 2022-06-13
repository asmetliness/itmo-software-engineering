package com.artefact.api.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class LoginRequest {

    private String email;
    private String password;
}
