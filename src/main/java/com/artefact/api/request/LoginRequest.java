package com.artefact.api.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class LoginRequest {

    @NotBlank(message = "Пожалуйста, укажите почту!")
    private String email;
    @NotBlank(message = "Пожалуйста, укажите пароль!")
    private String password;
}
