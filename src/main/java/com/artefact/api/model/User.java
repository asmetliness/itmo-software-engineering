package com.artefact.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "middle_name")
    private String middleName;

    @ManyToOne(targetEntity = Role.class, optional = false)
    @JoinColumn(name="role_id", nullable = false, insertable = false, updatable = false)
    private Role role;

    @Column(name="role_id")
    private Long roleId;

    @Column(name = "password_hash")
    @JsonIgnore
    private String passwordHash;
}
