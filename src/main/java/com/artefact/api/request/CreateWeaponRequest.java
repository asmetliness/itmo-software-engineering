package com.artefact.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class CreateWeaponRequest {

    private String title;
    private String description;
    private BigDecimal price;
}
