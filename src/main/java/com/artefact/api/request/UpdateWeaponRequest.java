package com.artefact.api.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class UpdateWeaponRequest {

    @NotNull(message = "Пожалуйста, укажите идентификатор оружия!")
    private Long id;

    @NotBlank(message = "Пожалуйста, укажите наименование!")
    private String title;
    private String description;
    @NotNull(message = "Пожалуйста, заполните цену!")
    private BigDecimal price;
}
