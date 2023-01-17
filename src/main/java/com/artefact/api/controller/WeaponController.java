package com.artefact.api.controller;


import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.User;
import com.artefact.api.model.Weapon;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.repository.WeaponRepository;
import com.artefact.api.repository.results.IWeaponResult;
import com.artefact.api.request.CreateWeaponRequest;
import com.artefact.api.request.SuggestWeaponRequest;
import com.artefact.api.request.UpdateWeaponRequest;
import com.artefact.api.request.WeaponRequest;
import com.artefact.api.response.WeaponResponse;
import com.artefact.api.utils.ApiErrors;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.Streams;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/api/weapon")
public class WeaponController {

    final private WeaponRepository weaponRepository;
    final private UserRepository userRepository;

    public WeaponController(WeaponRepository weaponRepository, UserRepository userRepository) {
        this.weaponRepository = weaponRepository;
        this.userRepository = userRepository;
    }


    @PostMapping
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> createWeapon(@Valid @RequestBody CreateWeaponRequest request) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();
        if(!user.getRole().equals(Role.WeaponDealer)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantCreate, HttpStatus.FORBIDDEN);
        }

        var weapon = new Weapon();
        weapon.setTitle(request.getTitle());
        weapon.setDescription(request.getDescription());
        weapon.setPrice(request.getPrice());
        weapon.setCreatedUserId(userId);
        weapon.setCreationDate(new Date());
        weapon.setStatusId(StatusIds.New);

        weaponRepository.save(weapon);

        return getWeaponById(weapon.getId());
    }

    @PutMapping
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> updateWeapon(@Valid @RequestBody UpdateWeaponRequest request) {
        var userId = Auth.userId();

        var weaponOpt = weaponRepository.findById(request.getId());
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getCreatedUserId().equals(userId) || !weapon.getStatusId().equals(StatusIds.New)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantEdit, HttpStatus.FORBIDDEN);
        }

        weapon.setTitle(request.getTitle());
        weapon.setDescription(request.getDescription());
        weapon.setPrice(request.getPrice());

        weaponRepository.save(weapon);
        return getWeaponById(weapon.getId());
    }

    @DeleteMapping("/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = WeaponResponse.class)))} ) })
    public ResponseEntity<Object> deleteWeapon(@PathVariable long id) {
        var userId = Auth.userId();

        var weapon = weaponRepository.findById(id);
        if(weapon.isEmpty()) {
            return getWeapons();
        }

        if(!weapon.get().getCreatedUserId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantDelete, HttpStatus.FORBIDDEN);
        }

        weaponRepository.deleteById(id);
        return getWeapons();
    }


    @GetMapping
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = WeaponResponse.class)))} ) })
    public ResponseEntity<Object> getWeapons() {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();

        var weapons = switch (user.getRole()) {
            case WeaponDealer -> weaponRepository.findByCreatedUserId(user.getId());
            case Stalker -> weaponRepository.findByAcquiredUserId(user.getId());
            default -> new ArrayList<IWeaponResult>();
        };

        return mapWeapons(weapons);
    }

    @GetMapping("/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> getWeaponById(@PathVariable long id) {

        var weaponOpt = weaponRepository.findByWeaponId(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        return new ResponseEntity<>(new WeaponResponse(weapon), HttpStatus.OK);
    }

    @GetMapping("/available")
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = WeaponResponse.class)))} ) })
    public ResponseEntity<Object> getAvailableWeapons() {

        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        var weapons = switch (user.getRole()) {
            case Stalker -> weaponRepository.findByStatusId(StatusIds.New);
            case Courier -> weaponRepository.findBySuggestedCourierId(userId);
            default -> new ArrayList<IWeaponResult>();
        };

        return mapWeapons(weapons);
    }

    @GetMapping("/requested")
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = WeaponResponse.class)))} ) })
    public ResponseEntity<Object> getRequestedWeapons() {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        var weapons = switch (user.getRole()) {
            case WeaponDealer -> weaponRepository.findRequestedWeapons(userId);
            case Stalker -> weaponRepository.findByRequestedUserId(userId);
            default -> new ArrayList<IWeaponResult>();
        };

        return mapWeapons(weapons);
    }

    private ResponseEntity<Object> mapWeapons(Iterable<IWeaponResult> weapons) {
        var result = Streams.from(weapons).map(WeaponResponse::new);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    //TODO: add notifications for request, confirm, decline

    @PostMapping("/buy/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> buyInstant(@PathVariable long id, @Valid @RequestBody WeaponRequest request) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        if(!user.getRole().equals(Role.Stalker)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantBuy, HttpStatus.FORBIDDEN);
        }

        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }

        var weapon = weaponOpt.get();
        weapon.setAcquiredUserId(userId);
        weapon.setStatusId(StatusIds.Acquired);
        weapon.setDeliveryAddress(request.getDeliveryAddress());

        weaponRepository.save(weapon);
        return getWeaponById(id);
    }

    @PostMapping("/request/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> requestWeapon(@PathVariable long id, @Valid @RequestBody WeaponRequest request) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        if(!user.getRole().equals(Role.Stalker)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantBuy, HttpStatus.FORBIDDEN);
        }

        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }

        var weapon = weaponOpt.get();
        weapon.setRequestedUserId(userId);
        weapon.setStatusId(StatusIds.Requested);
        weapon.setDeliveryAddress(request.getDeliveryAddress());

        weaponRepository.save(weapon);
        return getWeaponById(id);
    }

    @PostMapping("/confirm/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> confirmWeapon(@PathVariable long id) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId);

        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();

        if(!canConfirm(user.get(), weapon)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantConfirm, HttpStatus.FORBIDDEN);
        }
        if(user.get().getRole().equals(Role.WeaponDealer)) {

            weapon.setAcquiredUserId(weapon.getRequestedUserId());
            weapon.setRequestedUserId(null);
            weapon.setStatusId(StatusIds.Acquired);
        }
        if(user.get().getRole().equals(Role.Stalker)) {
            weapon.setStatusId(StatusIds.Completed);
        }

        weaponRepository.save(weapon);

        return getWeaponById(id);
    }

    private Boolean canConfirm(User user, Weapon weapon) {
        if(user.getRole().equals(Role.WeaponDealer)) {
            return weapon.getCreatedUserId().equals(user.getId())
                    && weapon.getStatusId().equals(StatusIds.Requested);
        }
        if(user.getRole().equals(Role.Stalker)) {
            return weapon.getAcquiredUserId().equals(user.getId())
                    && weapon.getStatusId().equals(StatusIds.Delivered);
        }
        return false;
    }

    @PostMapping("/decline/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> declineWeapon(@PathVariable long id) {
        var userId = Auth.userId();
        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getCreatedUserId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantDecline, HttpStatus.FORBIDDEN);
        }
        weapon.setRequestedUserId(null);
        weapon.setStatusId(StatusIds.New);
        weapon.setDeliveryAddress(null);
        weaponRepository.save(weapon);
        return getWeaponById(id);
    }


    @PostMapping("/suggest")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> suggestCourier(@Valid @RequestBody SuggestWeaponRequest request) {
        var userId = Auth.userId();
        var weaponOpt = weaponRepository.findById(request.getWeaponId());
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getCreatedUserId().equals(userId) || !weapon.getStatusId().equals(StatusIds.Acquired)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantSuggest, HttpStatus.FORBIDDEN);
        }

        var suggestedUser = userRepository.findById(request.getUserId());
        if(suggestedUser.isEmpty() || !suggestedUser.get().getRole().equals(Role.Courier)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantSuggestToUser, HttpStatus.FORBIDDEN);
        }

        weapon.setSuggestedCourierId(request.getUserId());
        weaponRepository.save(weapon);

        return getWeaponById(request.getWeaponId());
    }

    @PostMapping("/courier/accept/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> acceptCourier(@PathVariable("id") long id) {
        var userId = Auth.userId();
        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getSuggestedCourierId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantAccept, HttpStatus.FORBIDDEN);
        }

        weapon.setSuggestedCourierId(null);
        weapon.setAcceptedCourierId(userId);
        weapon.setStatusId(StatusIds.Sent);

        weaponRepository.save(weapon);

        return getWeaponById(id);
    }

    @PostMapping("/courier/decline/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> declineCourier(@PathVariable("id") long id) {
        var userId = Auth.userId();
        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getSuggestedCourierId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantDecline, HttpStatus.FORBIDDEN);
        }

        weapon.setSuggestedCourierId(null);
        weapon.setAcceptedCourierId(null);
        weapon.setStatusId(StatusIds.Acquired);
        weaponRepository.save(weapon);
        return getWeaponById(id);
    }

    @PostMapping("/courier/deliver/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = WeaponResponse.class))} ) })
    public ResponseEntity<Object> deliverCourier(@PathVariable("id") long id) {
        var userId = Auth.userId();
        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Weapon.NotFound, HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getAcceptedCourierId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Weapon.CantDeliver, HttpStatus.FORBIDDEN);
        }
        weapon.setStatusId(StatusIds.Delivered);
        weaponRepository.save(weapon);

        return getWeaponById(id);
    }


}
