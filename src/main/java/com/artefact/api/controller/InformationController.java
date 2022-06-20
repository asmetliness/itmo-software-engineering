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
    private final InformationRepository infoRepository;

    private final UserRepository userRepository;

    private final OrderStatusRepository orderStatusRepository;

    private final RoleRepository roleRepository;

    private final NotificationRepository notificationRepository;

    public InformationController(InformationRepository infoRepository,
                                 UserRepository userRepository,
                                 OrderStatusRepository orderStatusRepository,
                                 RoleRepository roleRepository,
                                 NotificationRepository notificationRepository) {
        this.infoRepository = infoRepository;
        this.userRepository = userRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.roleRepository = roleRepository;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    public ResponseEntity<Object> CreateInformation(@RequestBody CreateInformationOrder request) {
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

    @GetMapping
    public ResponseEntity<Iterable<InformationResponse>> GetInformationList() {
        String userId = (String) getContext().getAuthentication().getPrincipal();

        Iterable<Information> information = infoRepository.getAll();
        ArrayList<InformationResponse> response = new ArrayList<>();

        for (Information info : information) {
            InformationResponse info_response = (InformationResponse) GetInformation(info.getId()).getBody();
            response.add(info_response);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> GetInformation(@PathVariable Long id) {
        InformationResponse response = null; // TODO: Добавить правильный ответ
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
