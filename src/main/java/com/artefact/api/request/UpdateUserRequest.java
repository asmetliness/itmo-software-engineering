package com.artefact.api.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class UpdateUserRequest {
    String firstName;
    String lastName;
    String middleName;
    String nickname;
}
