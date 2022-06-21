package com.artefact.api.response;

import com.artefact.api.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class InformationResponse {
    private Long id;
    private String title;
    private String description;
    private String information;
    private Double price;
    private Date creationDate;
    private User createdUser;
    private User acceptedUser;
}
