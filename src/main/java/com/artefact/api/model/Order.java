package com.artefact.api.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = Artifact.class, optional = false)
    @JoinColumn(name="artifact_id", nullable = false, insertable = false, updatable = false)
    private Artifact artifact;

    @Column(name = "artifact_id")
    private Long artifactId;

    @Column
    private Double price;

    @Column(name = "completion_date")
    private Date completionDate;

    @ManyToOne(targetEntity = User.class, optional = true)
    @JoinColumn(name="created_user_id", nullable = false, insertable = false, updatable = false)
    private User createdUser;

    @Column(name = "created_user_id")
    private Long createdUserId;

    @ManyToOne(targetEntity = User.class, optional = true)
    @JoinColumn(name="accepted_user_id", nullable = false, insertable = false, updatable = false)
    private User acceptedUser;

    @Column(name = "accepted_user_id")
    private Long acceptedUserId;

    @ManyToOne(targetEntity = User.class, optional = true)
    @JoinColumn(name="assigned_user_id", nullable = false, insertable = false, updatable = false)
    private User assignedUser;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @ManyToOne(targetEntity = User.class, optional = true)
    @JoinColumn(name="suggested_user_id", nullable = false, insertable = false, updatable = false)
    private User suggestedUser;

    @Column(name = "suggested_user_id")
    private Long suggestedUserId;

    @ManyToOne(targetEntity = OrderStatus.class, optional = false)
    @JoinColumn(name="status_id", nullable = false, insertable = false, updatable = false)
    private OrderStatus status;

    @Column(name = "status_id")
    private Long statusId;
}
