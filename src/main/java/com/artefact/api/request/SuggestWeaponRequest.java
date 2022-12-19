package com.artefact.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class SuggestWeaponRequest {
    private long weaponId;
    private long userId;
}
