package com.example.carrotstatebackend.repositories.persons;

import com.example.carrotstatebackend.entities.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAgentRepository extends JpaRepository<Agent, Long> {
    Optional<List<Agent>> findAllByRealState_Id(Long manager);
    Optional<Agent> findByEmail(String email);
}
