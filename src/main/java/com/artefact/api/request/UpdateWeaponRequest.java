package com.artefact.api.request;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class UpdateWeaponRequest extends CreateWeaponRequest{

    @NotNull(message = "Пожалуйста, укажите идентификатор оружия!")
    private long id;
}
