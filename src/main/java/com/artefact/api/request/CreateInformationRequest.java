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
@AllArgsConstructor
@ToString
public class CreateInformationRequest {

    @NotBlank(message = "Пожалуйста, укажите заголовок!")
    private String title;
    @NotBlank(message = "Пожалуйста, заполните описание!")
    private String description;
    @NotBlank(message = "Пожалуйста, заполните продаваемую информацию!")
    private String information; // Информацию, которую купили. Отдавать только при покупке.

    @NotNull(message = "Пожалуйста, заполните цену!")
    private BigDecimal price;
}
