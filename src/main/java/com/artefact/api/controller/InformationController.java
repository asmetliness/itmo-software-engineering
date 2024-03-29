package com.artefact.api.controller;

import com.artefact.api.consts.Role;
import com.artefact.api.consts.StatusIds;
import com.artefact.api.model.Information;
import com.artefact.api.model.Notification;
import com.artefact.api.repository.*;
import com.artefact.api.repository.results.IInformationResult;
import com.artefact.api.request.CreateInformationRequest;
import com.artefact.api.request.UpdateInformationRequest;
import com.artefact.api.response.InformationResponse;
import com.artefact.api.utils.ApiErrors;
import com.artefact.api.utils.Auth;
import com.artefact.api.utils.NotificationMessages;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/api/information")
public class InformationController {
    final private InformationRepository infoRepository;
    final private UserRepository userRepository;

    final private NotificationRepository notificationRepository;

    public InformationController(InformationRepository infoRepository,
                                 UserRepository userRepository,
                                 NotificationRepository notificationRepository) {
        this.infoRepository = infoRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = InformationResponse.class))} ) })
    public ResponseEntity<Object> createInformation(@Valid @RequestBody CreateInformationRequest request) {
        var userId = Auth.userId();

        var user = userRepository.findById(userId).get();
        if(!user.getRole().equals(Role.Informer)) {
            return new ResponseEntity<>(ApiErrors.Information.CreateError, HttpStatus.FORBIDDEN);
        }
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
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = InformationResponse.class))} ) })
    public ResponseEntity<Object> updateInformation(@Valid @RequestBody UpdateInformationRequest request) {
        var userId = Auth.userId();
        var information = infoRepository.findById(request.getId());
        if(information.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Information.NotFound, HttpStatus.NOT_FOUND);
        }
        var info = information.get();
        if(info.getCreatedUserId() != userId || info.getStatusId() != StatusIds.New){
            return new ResponseEntity<>(ApiErrors.Information.AccessViolation, HttpStatus.FORBIDDEN);
        }

        info.setTitle(request.getTitle());
        info.setDescription(request.getDescription());
        info.setInformation(request.getInformation());
        info.setPrice(request.getPrice());

        infoRepository.save(info);

        return getInformation(info.getId());
    }

    @DeleteMapping("/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = InformationResponse.class)))} ) })
    public ResponseEntity<Object> deleteInformation(@PathVariable long id) {
        var userId = Auth.userId();
        var information = infoRepository.findById(id);

        if(information.isEmpty()) {
            return getInformationList();
        }
        var info = information.get();
        if(info.getCreatedUserId() != userId || info.getStatusId() != StatusIds.New) {
            return new ResponseEntity<>(ApiErrors.Information.AccessViolation, HttpStatus.FORBIDDEN);
        }

        infoRepository.deleteById(id);

        return getInformationList();
    }

    @GetMapping
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = InformationResponse.class)))} ) })
    public ResponseEntity<Object> getInformationList() {
        var userId = Auth.userId();

        var user = userRepository.findById(userId);

        var information = switch (user.get().getRole()) {
            case Informer -> infoRepository.findByCreatedUser(userId);
            case Stalker -> infoRepository.findByAcquiredUser(userId);
            default -> new ArrayList<IInformationResult>();
        };

        return getIterableResponseEntity(information, false);
    }

    @GetMapping("/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = InformationResponse.class))} ) })
    public ResponseEntity<Object> getInformation(@PathVariable Long id) {
        var userId = Auth.userId();

        var infoOpt = infoRepository.findByInformationId(id);
        if (infoOpt.isEmpty())
            return new ResponseEntity<>(ApiErrors.Information.NotFound, HttpStatus.NOT_FOUND);

        var info = infoOpt.get();

        var showInfo = info.getAcquiredUser() != null && info.getAcquiredUser().getId().equals(userId);
        showInfo |= info.getCreatedUser().getId().equals(userId);

        if(!showInfo) {
            info.getInformation().setInformation(null);
        }
        var response = new InformationResponse(info);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/available")
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = InformationResponse.class)))} ) })
    public ResponseEntity<Object> getAvailableList() {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        var information = switch (user.getRole()) {
            case Stalker -> infoRepository.findByStatus(StatusIds.New);
            default -> new ArrayList<IInformationResult>();
        };
        return getIterableResponseEntity(information, true);
    }


    @PostMapping("/buy/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = InformationResponse.class))} ) })
    public ResponseEntity<Object> buyInstant(@PathVariable long id) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        if(!user.getRole().equals(Role.Stalker)) {
            return new ResponseEntity<>(ApiErrors.Information.UnauthorizedRole, HttpStatus.FORBIDDEN);
        }
        var informationOpt = infoRepository.findById(id);
        if(informationOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Information.NotFound, HttpStatus.NOT_FOUND);
        }

        var information = informationOpt.get();
        information.setAcquiredUserId(userId);
        information.setStatusId(StatusIds.Acquired);

        notificationRepository.save(new Notification(NotificationMessages.Information.Bought,
                information.getCreatedUserId(),
                information));

        infoRepository.save(information);
        return getInformation(id);
    }

    @GetMapping("/requested")
    @ApiResponses(value = { @ApiResponse(content = { @Content(array = @ArraySchema( schema = @Schema(implementation = InformationResponse.class)))} ) })
    public ResponseEntity<Object> getRequestedInformation() {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        var info = switch (user.getRole()) {
            case Informer -> infoRepository.findRequestedInformation(userId);
            case Stalker -> infoRepository.findByRequestedUserId(userId);
            default -> new ArrayList<IInformationResult>();
        };

        return getIterableResponseEntity(info, user.getRole() != Role.Informer);
    }

    @PostMapping("/request/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = InformationResponse.class))} ) })
    public ResponseEntity<Object> requestInformation(@PathVariable long id) {
        var userId = Auth.userId();
        var user = userRepository.findById(userId).get();

        if(!user.getRole().equals(Role.Stalker)) {
            return new ResponseEntity<>(ApiErrors.Information.UnauthorizedRole, HttpStatus.FORBIDDEN);
        }

        var informationOpt = infoRepository.findById(id);
        if(informationOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Information.NotFound, HttpStatus.NOT_FOUND);
        }

        var information = informationOpt.get();
        information.setRequestedUserId(userId);
        information.setStatusId(StatusIds.Requested);

        infoRepository.save(information);
        return getInformation(id);
    }

    @PostMapping("/confirm/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = InformationResponse.class))} ) })
    public ResponseEntity<Object> confirmInformation(@PathVariable long id) {
        var userId = Auth.userId();
        var informationOpt = infoRepository.findById(id);
        if(informationOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Information.NotFound, HttpStatus.NOT_FOUND);
        }
        var information = informationOpt.get();
        if(!information.getCreatedUserId().equals(userId)) {
            return new ResponseEntity<>(ApiErrors.Information.AccessViolation, HttpStatus.FORBIDDEN);
        }

        information.setAcquiredUserId(information.getRequestedUserId());
        information.setRequestedUserId(null);
        information.setStatusId(StatusIds.Acquired);

        infoRepository.save(information);

        return getInformation(id);
    }

    @PostMapping("/decline/{id}")
    @ApiResponses(value = { @ApiResponse(content = { @Content(schema = @Schema(implementation = InformationResponse.class))} ) })
    public ResponseEntity<Object> declineInformation(@PathVariable long id) {
        var userId = Auth.userId();
        var informationOpt = infoRepository.findById(id);
        if(informationOpt.isEmpty()) {
            return new ResponseEntity<>(ApiErrors.Information.NotFound, HttpStatus.NOT_FOUND);
        }
        var information = informationOpt.get();
        if(!information.getCreatedUserId().equals(userId) || information.getAcquiredUser() != null) {
            return new ResponseEntity<>(ApiErrors.Information.AccessViolation, HttpStatus.FORBIDDEN);
        }

        information.setRequestedUserId(null);
        information.setStatusId(StatusIds.New);

        infoRepository.save(information);
        return getInformation(id);
    }



    private ResponseEntity<Object> getIterableResponseEntity(Iterable<IInformationResult> information, Boolean hideInfo) {

        var response = new ArrayList<InformationResponse>();
        for (var info : information) {
            if(hideInfo) {
                info.getInformation().setInformation(null);
            }
            response.add(new InformationResponse(info));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
