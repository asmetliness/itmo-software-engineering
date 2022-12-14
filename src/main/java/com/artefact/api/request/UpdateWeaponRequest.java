package com.artefact.api.request;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateWeaponRequest extends CreateWeaponRequest{

    private long id;
}
