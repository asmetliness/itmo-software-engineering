package com.artefact.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Artifact.class, optional = false)
    @JoinColumn(name="artifact_id", nullable = false, insertable = false, updatable = false)
    private Artifact artifact;

    @Column(name = "artifact_id", nullable = false)
    private Long artifactId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "completion_date", columnDefinition = "DATE")
    private Date completionDate;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="created_user_id", nullable = false, insertable = false, updatable = false)
    private User createdUser;

    @Column(name = "created_user_id", nullable = false)
    private Long createdUserId;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="accepted_user_id", insertable = false, updatable = false)
    private User acceptedUser;

    @Column(name = "accepted_user_id")
    private Long acceptedUserId;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="assigned_user_id", insertable = false, updatable = false)
    private User assignedUser;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="suggested_user_id", insertable = false, updatable = false)
    private User suggestedUser;

    @Column(name = "suggested_user_id")
    private Long suggestedUserId;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="accepted_courier_id", insertable = false, updatable = false)
    private User acceptedCourier;

    @Column(name = "accepted_courier_id")
    private Long acceptedCourierId;

    @JsonIgnore
    @ManyToOne(targetEntity = Status.class, optional = false)
    @JoinColumn(name="status_id", nullable = false, insertable = false, updatable = false)
    private Status status;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @Column(name = "delivery_address")
    private String deliveryAddress;
}
