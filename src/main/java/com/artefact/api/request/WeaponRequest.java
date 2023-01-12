package com.artefact.api.request;


import com.artefact.api.response.WeaponResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class WeaponRequest {

    @NotBlank(message = "Пожалуйста, укажите адрес доставки!")
    String deliveryAddress;

    public WeaponRequest(){}
}
