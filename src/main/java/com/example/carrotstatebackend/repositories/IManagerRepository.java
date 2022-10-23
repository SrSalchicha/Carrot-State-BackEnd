package com.example.carrotstatebackend.repositories;

import com.example.carrotstatebackend.entities.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IManagerRepository extends JpaRepository<Manager, Long> { }