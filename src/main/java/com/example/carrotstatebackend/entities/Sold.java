package com.example.carrotstatebackend.entities;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import javax.persistence.*;

@Entity
@Table
@Getter @Setter
public class Sold{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne Owner owner;

    @ManyToOne Agent agent;

    @ManyToOne Premise premise;

    @ManyToOne Plot plot;

    @ManyToOne House house;


}