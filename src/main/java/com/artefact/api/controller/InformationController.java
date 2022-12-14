package com.artefact.api.controller;

import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Information;
import com.artefact.api.repository.*;
import com.artefact.api.repository.results.IInformationResult;
import com.artefact.api.request.CreateInformationRequest;
import com.artefact.api.request.UpdateInformationRequest;
import com.artefact.api.response.InformationResponse;
import com.artefact.api.utils.Auth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Controller
@RequestMapping("/api/information")
public class InformationController {
    final private InformationRepository infoRepository;

    final private UserRepository userRepository;


    public InformationController(InformationRepository infoRepository,
                                 UserRepository userRepository) {
        this.infoRepository = infoRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<InformationResponse> createInformation(@RequestBody CreateInformationRequest request) {
        var userId = Auth.userId();

        Information info = new Information();
        info.setCreatedUserId(userId);
        info.setStatusId(StatusIds.New);
        info.setTitle(request.getTitle());
        info.setDescription(request.getDescription());
        info.setInformation(request.getInformation());
        info.setPrice(request.getPrice());
        info.setCreationDate(new Date());

        infoRepository.save(info);

        return getInformation(info.getId());
    }

    @PutMapping
    public ResponseEntity<InformationResponse> updateInformation(@RequestBody UpdateInformationRequest request) {
        var userId = Auth.userId();
        var information = infoRepository.findById(request.getId());
        if(information.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        var info = information.get();
        if(info.getCreatedUserId() != userId){
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        if(info.getStatusId() != StatusIds.New) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        info.setTitle(request.getTitle());
        info.setDescription(request.getDescription());
        info.setInformation(request.getInformation());
        info.setPrice(request.getPrice());

        infoRepository.save(info);

        return getInformation(info.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Iterable<InformationResponse>> deleteInformation(@PathVariable long id) {
        var userId = Auth.userId();
        var information = infoRepository.findById(id);

        if(information.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        var info = information.get();
        if(info.getCreatedUserId() != userId) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        if(info.getStatusId() != StatusIds.New) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        infoRepository.deleteById(id);

        return getInformationList();
    }

    @GetMapping("/available")
    public ResponseEntity<Iterable<InformationResponse>> getAvailableList() {
        var information = infoRepository.findAllNotAccepted();
        return getIterableResponseEntity(information, true);
    }

    private ResponseEntity<Iterable<InformationResponse>> getIterableResponseEntity(Iterable<IInformationResult> information, Boolean hideInfo) {

        var response = new ArrayList<InformationResponse>();
        for (var info : information) {
            if(hideInfo) {
                info.getInformation().setInformation(null);
            }
            response.add(new InformationResponse(
                    info.getInformation(),
                    info.getCreatedUser(),
                    info.getAcceptedUser()
            ));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Iterable<InformationResponse>> getInformationList() {
        var userId = Auth.userId();

        var user = userRepository.findById(userId);

        var information = switch (user.get().getRole()) {
            case Informer -> infoRepository.findByCreatedUser(userId);
            case Stalker -> infoRepository.findByAcceptedUser(userId);
            default -> new ArrayList<IInformationResult>();
        };

        return getIterableResponseEntity(information, false);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InformationResponse> getInformation(@PathVariable Long id) {
        var userId = Auth.userId();

        var infoOpt = infoRepository.findById(id);
        if (infoOpt.isEmpty())
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        var info = infoOpt.get();

        if(!Objects.equals(info.getAcceptedUserId(), userId) && !Objects.equals(info.getCreatedUserId(), userId)) {
            info.setInformation(null);
        }
        var response = new InformationResponse(
                info,
                info.getCreatedUser(),
                info.getAcceptedUser()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/buy/{id}")
    public ResponseEntity<InformationResponse> buyInformation(@PathVariable Long id) {
        var userId = Auth.userId();

        var infoOpt = infoRepository.findById(id);
        if (infoOpt.isEmpty())
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        var info = infoOpt.get();
        if (info.getCreatedUserId() == userId) // Запрещаем покупать человеку, который этот заказ создал
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        info.setAcceptedUserId(userId);
        infoRepository.save(info);

        return getInformation(info.getId());
    }
}
