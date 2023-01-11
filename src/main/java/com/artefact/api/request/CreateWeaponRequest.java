package com.artefact.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class CreateWeaponRequest {

    @NotBlank(message = "Пожалуйста, укажите наименование!")
    private String title;
    private String description;
    @NotNull(message = "Пожалуйста, заполните цену!")
    private BigDecimal price;
}
