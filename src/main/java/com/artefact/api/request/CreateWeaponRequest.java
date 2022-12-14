package com.artefact.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateWeaponRequest {

    private String title;
    private String description;
    private Double price;
}
