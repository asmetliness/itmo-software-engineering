package com.artefact.api.model;

import lombok.Data;

import javax.persistence.*;
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
    private Double price;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "created_user_id", nullable = false)
    private Long createdUserId;

    @Column(name = "accepted_user_id")
    private Long acceptedUserId; // Кто купил

    @Column(name = "status_id", nullable = false)
    private Long statusId; // Мб не нужно, т.к. если `acceptedUserId` не null, то значит, что кто-то инфу купил. B статус не нужон
}
