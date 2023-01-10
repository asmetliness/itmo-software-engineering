package com.artefact.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    public Notification(){}

    public Notification(String message, Long userId, Long orderId) {
        this.setMessage(message);
        this.setUserId(userId);
        this.setOrderId(orderId);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String message;

    @Column(name = "was_read")
    private boolean wasRead;

    @Column(name = "user_id", nullable = false)
    @JsonIgnore
    private Long userId;

    @Column(name="order_id")
    private Long orderId;

    @Column(name="weapon_order_id")
    private Long weaponOrderId;

    @Column(name="information_order_id")
    private Long informationOrderId;
}
