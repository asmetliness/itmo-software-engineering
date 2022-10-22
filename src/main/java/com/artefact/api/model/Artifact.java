package com.artefact.api.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "artifact")
public class Artifact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private double price;
}
