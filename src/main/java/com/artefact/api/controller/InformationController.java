package com.artefact.api.controller;

import com.artefact.api.consts.OrderStatusIds;
import com.artefact.api.consts.RoleNames;
import com.artefact.api.model.Information;
import com.artefact.api.model.Role;
import com.artefact.api.model.User;
import com.artefact.api.repository.*;
import com.artefact.api.request.CreateInformationOrder;
import com.artefact.api.response.InformationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Controller
@RequestMapping("/api/information")
public class InformationController {
    private InformationRepository infoRepository;

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    public InformationController(InformationRepository infoRepository,
                                 UserRepository userRepository,
                                 RoleRepository roleRepository) {
        this.infoRepository = infoRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostMapping
    public ResponseEntity<InformationResponse> CreateInformation(@RequestBody CreateInformationOrder request) {
        String userId = (String) getContext().getAuthentication().getPrincipal();

        Information info = new Information();
        info.setCreatedUserId(Long.parseLong(userId));
        info.setStatusId(OrderStatusIds.NewOrder);
        info.setTitle(request.getTitle());
        info.setDescription(request.getDescription());
        info.setInformation(request.getInformation());
        info.setPrice(request.getPrice());
        info.setCreationDate(new Date());

        infoRepository.save(info);

        return GetInformation(info.getId());
    }

    @GetMapping("/available")
    public ResponseEntity<Iterable<InformationResponse>> GetAvailableList() {
        Iterable<Information> information = infoRepository.findByAcceptedUser(null);
        return GetIterableResponseEntity(information, true);
    }

    private ResponseEntity<Iterable<InformationResponse>> GetIterableResponseEntity(Iterable<Information> information, Boolean hideInfo) {
        ArrayList<InformationResponse> response = new ArrayList<>();
        for (Information info : information) {
            InformationResponse info_response = GetInformation(info.getId()).getBody();
            if(hideInfo) {
                info_response.setInformation(null);
            }
            response.add(info_response);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Iterable<InformationResponse>> GetInformationList() {
        String userIdStr = (String) getContext().getAuthentication().getPrincipal();
        long userId = Long.parseLong(userIdStr);

        Optional<User> user = userRepository.findById(userId);
        Optional<Role> roleOpt = roleRepository.findById(user.get().getRoleId());
        Role role = roleOpt.get();

        Iterable<Information> information;
        if (role.getName().equals(RoleNames.Informer)) {
            information = infoRepository.findByCreatedUser(userId);
        } else {
            information = infoRepository.findByAcceptedUser(userId);
        }

        return GetIterableResponseEntity(information, false);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InformationResponse> GetInformation(@PathVariable Long id) {
        String userIdStr = (String) getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        Optional<Information> infoOpt = infoRepository.findById(id);
        if (!infoOpt.isPresent())
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        Information info = infoOpt.get();
        Optional<User> createdUser = userRepository.findById(info.getCreatedUserId()); // Not nullabel
        User acceptedUser = null;
        if(info.getAcceptedUserId() != null) {
            acceptedUser = userRepository.findById(info.getAcceptedUserId()).orElse(null);
        }

        if(info.getAcceptedUserId() != userId && info.getCreatedUserId() != userId) {
            info.setInformation(null);
        }

        InformationResponse response = new InformationResponse(
                info.getId(),
                info.getTitle(),
                info.getDescription(),
                info.getInformation(),
                info.getPrice(),
                info.getCreationDate(),
                createdUser.get(),
                acceptedUser
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/buy/{id}")
    public ResponseEntity<InformationResponse> BuyInformation(@PathVariable Long id) {
        String userIdStr = (String) getContext().getAuthentication().getPrincipal();
        long userId = Long.parseLong(userIdStr);

        Optional<Information> infoOpt = infoRepository.findById(id);
        if (!infoOpt.isPresent())
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        Information info = infoOpt.get();
        if (info.getCreatedUserId() == userId) // Запрещаем покупать человеку, который этот заказ создал
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        info.setAcceptedUserId(userId);
        infoRepository.save(info);

        return GetInformation(info.getId());
    }
}
