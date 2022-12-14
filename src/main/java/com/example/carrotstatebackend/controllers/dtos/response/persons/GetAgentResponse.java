package com.example.carrotstatebackend.controllers.dtos.response.persons;

import com.example.carrotstatebackend.entities.enums.State;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Builder
public class GetAgentResponse {
    private Long id;
    private String name;
    private String email;
    private String profilePicture;
    private Integer numberOfSales;
    private Integer numberOfProperties;
    private State state;
}
