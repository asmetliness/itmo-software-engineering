package com.artefact.api.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="order_status")
public class OrderStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
}
