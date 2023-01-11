package com.artefact.api.request;

import com.artefact.api.consts.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class RegisterRequest {

    @NotBlank(message = "Пожалуйста, укажите почту!")
    private String email;
    @NotBlank(message = "Пожалуйста, укажите пароль!")
    private String password;
    @NotNull(message = "Пожалуйста, укажите роль!")
    private Role role;
}
