package com.example.carrotstatebackend.controllers.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Builder
public class GetLoginResponse {
    private Boolean success;
    private Object logged;
}
