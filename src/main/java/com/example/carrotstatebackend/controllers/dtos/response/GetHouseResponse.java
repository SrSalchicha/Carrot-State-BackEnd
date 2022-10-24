package com.example.carrotstatebackend.controllers.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GetHouseResponse {

    private Long id;

    private String location;

    private String description;

    private Integer bathroomNum;

    private Integer rooms;

    private Integer floors;
}