package com.artefact.api.response;


import com.artefact.api.model.Status;
import com.artefact.api.model.User;
import com.artefact.api.model.Weapon;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeaponResponse {

    private Weapon weapon;
    private User createdUser;
    private User requestedUser;
    private User acquiredUser;
    private User suggestedCourier;
    private User acceptedCourier;
    private Status status;

    public WeaponResponse(Weapon weapon) {
        this.weapon = weapon;
        this.createdUser = weapon.getCreatedUser();
        this.requestedUser = weapon.getRequestedUser();
        this.acquiredUser = weapon.getAcquiredUser();
        this.suggestedCourier = weapon.getSuggestedCourier();
        this.acceptedCourier = weapon.getAcceptedCourier();
        this.status = weapon.getStatus();
    }

}
