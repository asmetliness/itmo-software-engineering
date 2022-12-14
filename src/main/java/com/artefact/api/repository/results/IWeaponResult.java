package com.artefact.api.repository.results;

import com.artefact.api.model.Status;
import com.artefact.api.model.User;
import com.artefact.api.model.Weapon;

public interface IWeaponResult {

    Weapon getWeapon();

    User getCreatedUser();
    User getRequestedUser();
    User getAcquiredUser();
    User getSuggestedCourier();
    User getAcceptedCourier();

    Status getStatus();

}
