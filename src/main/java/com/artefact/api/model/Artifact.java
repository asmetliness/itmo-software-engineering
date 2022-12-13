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

    @Column(name = "image_path")
    private String imagePath;

    @Column(name="average_days")
    private int averageDays;
}
