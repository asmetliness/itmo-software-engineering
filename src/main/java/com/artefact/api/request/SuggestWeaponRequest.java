package com.artefact.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class SuggestWeaponRequest {

    @NotNull(message = "Пожалуйста, укажите идентификатор оружия!")
    private Long weaponId;
    @NotNull(message = "Пожалуйста, укажите идентификатор пользователя!")
    private Long userId;
}
