package com.artefact.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class UpdateInformationRequest {

    @NotNull(message = "Пожалуйста, укажите идентификатор информации!")
    private Long id;
    @NotBlank(message = "Пожалуйста, укажите заголовок!")
    private String title;
    @NotBlank(message = "Пожалуйста, укажите описание!")
    private String description;
    @NotBlank(message = "Пожалуйста, укажите информацию!")
    private String information; // Информацию, которую купили. Отдавать только при покупке.
    @NotNull(message = "Пожалуйста, укажите цену!")
    private BigDecimal price;
}
