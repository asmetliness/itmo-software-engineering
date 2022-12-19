package com.artefact.api.controller;


import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Weapon;
import com.artefact.api.repository.UserRepository;
import com.artefact.api.repository.WeaponRepository;
import com.artefact.api.repository.results.IWeaponResult;
import com.artefact.api.request.CreateWeaponRequest;
import com.artefact.api.request.SuggestWeaponRequest;
import com.artefact.api.request.UpdateWeaponRequest;
import com.artefact.api.response.WeaponResponse;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;

@Controller
@RequestMapping("/api/weapon")
public class WeaponController {

    final private WeaponRepository weaponRepository;
    final private UserRepository userRepository;

    public WeaponController(WeaponRepository weaponRepository, UserRepository userRepository) {
        this.weaponRepository = weaponRepository;
        this.userRepository = userRepository;
    }


    @PostMapping
    public ResponseEntity<Object> createWeapon(@RequestBody CreateWeaponRequest request) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();
        if(!user.getRole().equals(Role.WeaponDealer)) {
            return new ResponseEntity<>("Выставлять оружие может только торговец оружием!", HttpStatus.FORBIDDEN);
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
    public ResponseEntity<Object> updateWeapon(@RequestBody UpdateWeaponRequest request) {
        var userId = Auth.userId();

        var weaponOpt = weaponRepository.findById(request.getId());
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>("Не удалось найти оружие по указанному Id", HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getCreatedUserId().equals(userId) || !weapon.getStatusId().equals(StatusIds.New)) {
            return new ResponseEntity<>("Вы не можете редактировать данное оружие!", HttpStatus.FORBIDDEN);
        }

        weapon.setTitle(request.getTitle());
        weapon.setDescription(request.getDescription());
        weapon.setPrice(request.getPrice());

        weaponRepository.save(weapon);
        return getWeaponById(weapon.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteWeapon(@PathVariable long id) {
        var userId = Auth.userId();

        var weapon = weaponRepository.findById(id);
        if(weapon.isEmpty() || !weapon.get().getCreatedUserId().equals(userId)) {
            return getWeapons();
        }

        weaponRepository.deleteById(id);
        return getWeapons();
    }


    @GetMapping
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
    public ResponseEntity<Object> getWeaponById(@PathVariable long id) {

        var weaponOpt = weaponRepository.findByWeaponId(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>("Оружие не найдено!", HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        return new ResponseEntity<>(new WeaponResponse(weapon), HttpStatus.OK);
    }

    @GetMapping("/available")
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
    @PostMapping("/request/{id}")
    public ResponseEntity<Object> requestWeapon(@PathVariable long id) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        if(!user.getRole().equals(Role.Stalker)) {
            return new ResponseEntity<>("Покупка оружия доступна только сталкерам!", HttpStatus.FORBIDDEN);
        }

        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>("Оружие не найдено!", HttpStatus.NOT_FOUND);
        }

        var weapon = weaponOpt.get();
        weapon.setRequestedUserId(userId);
        weapon.setStatusId(StatusIds.Requested);

        weaponRepository.save(weapon);
        return getWeaponById(id);
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<Object> confirmWeapon(@PathVariable long id) {
        var userId = Auth.userId();
        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>("Оружие не найдено!", HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getCreatedUserId().equals(userId)) {
            return new ResponseEntity<>("Вы не можете подтвердить данный заказ", HttpStatus.FORBIDDEN);
        }

        weapon.setAcquiredUserId(weapon.getRequestedUserId());
        weapon.setRequestedUserId(null);
        weapon.setStatusId(StatusIds.Acquired);

        weaponRepository.save(weapon);

        return getWeaponById(id);
    }

    @PostMapping("/decline/{id}")
    public ResponseEntity<Object> declineWeapon(@PathVariable long id) {
        var userId = Auth.userId();
        var weaponOpt = weaponRepository.findById(id);
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>("Оружие не найдено!", HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getCreatedUserId().equals(userId)) {
            return new ResponseEntity<>("Вы не можете отклонить данный заказ", HttpStatus.FORBIDDEN);
        }
        weapon.setRequestedUserId(null);
        weapon.setStatusId(StatusIds.New);
        weaponRepository.save(weapon);
        return getWeaponById(id);
    }


    @PostMapping("/suggest")
    public ResponseEntity<Object> suggestCourier(@RequestBody SuggestWeaponRequest request) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        if(!user.getRole().equals(Role.WeaponDealer)) {
            return new ResponseEntity<>("Вы не можете предложить это оружие курьеру!", HttpStatus.FORBIDDEN);
        }
        var weaponOpt = weaponRepository.findById(request.getWeaponId());
        if(weaponOpt.isEmpty()) {
            return new ResponseEntity<>("Оружие не найдено!", HttpStatus.NOT_FOUND);
        }
        var weapon = weaponOpt.get();
        if(!weapon.getCreatedUserId().equals(userId)) {
            return new ResponseEntity<>("Вы не можете предложить это оружие курьеру!", HttpStatus.FORBIDDEN);
        }

        var suggestedUser = userRepository.findById(request.getUserId());
        if(suggestedUser.isEmpty() || !suggestedUser.get().getRole().equals(Role.Courier)) {
            return new ResponseEntity<>("Вы не можете предложить доставку этому пользователю!", HttpStatus.FORBIDDEN);
        }

        weapon.setSuggestedCourierId(request.getUserId());
        weaponRepository.save(weapon);

        return getWeaponById(request.getWeaponId());

    }
}
