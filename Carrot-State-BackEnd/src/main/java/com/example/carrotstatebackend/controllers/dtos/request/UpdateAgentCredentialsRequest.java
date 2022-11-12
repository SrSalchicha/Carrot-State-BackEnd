package com.example.carrotstatebackend.controllers.dtos.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter @Setter
public class UpdateAgentCredentialsRequest {

    @Email @NotNull
    private String mail;

    @NotNull
    private String password;
}
