package com.artefact.api.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "weapon")
public class Weapon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column()
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, name = "creation_date")
    private Date creationDate;

    @Column(nullable = false, name = "created_user_id")
    private Long createdUserId;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="created_user_id", nullable = false, insertable = false, updatable = false)
    private User createdUser;

    @Column(name = "requested_user_id")
    private Long requestedUserId;
    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="requested_user_id", insertable = false, updatable = false)
    private User requestedUser;

    @Column(name = "acquired_user_id")
    private Long acquiredUserId;
    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="acquired_user_id", insertable = false, updatable = false)
    private User acquiredUser;

    @Column(name = "suggested_courier_id")
    private Long suggestedCourierId;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="suggested_courier_id", insertable = false, updatable = false)
    private User suggestedCourier;

    @Column(name = "accepted_courier_id")
    private Long acceptedCourierId;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="accepted_courier_id", insertable = false, updatable = false)
    private User acceptedCourier;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @JsonIgnore
    @ManyToOne(targetEntity = Status.class, optional = false)
    @JoinColumn(name="status_id", nullable = false, insertable = false, updatable = false)
    private Status status;


    @Column(name = "delivery_address")
    private String deliveryAddress;
}
