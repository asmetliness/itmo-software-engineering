package com.artefact.api.controller;

import com.artefact.api.consts.OrderStatusIds;
import com.artefact.api.model.Information;
import com.artefact.api.repository.InformationRepository;
import com.artefact.api.request.CreateInformationOrder;
import com.artefact.api.response.InformationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Controller
@RequestMapping("/api/information")
public class InformationController {
    @Autowired
    private InformationRepository infoRepository;

    @PostMapping
    public ResponseEntity<Object> CreateOrder(@RequestBody CreateInformationOrder request) {
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

        InformationResponse response = null; // TODO: Добавить правильный ответ
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
