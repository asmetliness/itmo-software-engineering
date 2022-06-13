package com.artefact.api.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;
    @Column(name="last_name")
    private String lastName;
    @Column(name="middle_name")
    private String middleName;
    @Column(name="role_id")
    private Long roleId;
    @Column
    private String login;
    @Column(name = "password_hash")
    private String passwordHash;

}
