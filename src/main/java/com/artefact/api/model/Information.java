package com.artefact.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "information")
public class Information {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String information; // Информацию, которую купили. Отдавать только при покупке.

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "creation_date", nullable = false, columnDefinition = "DATE")
    private Date creationDate;

    @Column(name = "created_user_id", nullable = false)
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

    @Column(name = "status_id", nullable = false)
    private Long statusId; // Мб не нужно, т.к. если `acceptedUserId` не null, то значит, что кто-то инфу купил. B статус не нужон
}
