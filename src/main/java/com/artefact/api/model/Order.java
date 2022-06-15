package com.artefact.api.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name="orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artifact_id")
    private Long artifactId;

    @Column
    private Double price;

    @Column(name = "completion_date")
    private Date completionDate;

    @Column(name="created_user_id")
    private Long createdUserId;

    @Column(name="accepted_user_id")
    private Long acceptedUserId;

    @Column(name="assigned_user_id")
    private Long assignedUserId;

    @Column(name="suggested_user_id")
    private Long suggestedUserId;

    @Column(name="status_id")
    private Long statusId;
}
