package com.artefact.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class UpdateInformationRequest {
    private long id;
    private String title;
    private String description;
    private String information; // Информацию, которую купили. Отдавать только при покупке.
    private BigDecimal price;
}
