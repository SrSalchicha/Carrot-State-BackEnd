package com.example.carrotstatebackend.repositories.properties;

import com.example.carrotstatebackend.entities.Agent;
import com.example.carrotstatebackend.entities.Client;
import com.example.carrotstatebackend.entities.House;
import com.example.carrotstatebackend.entities.enums.CityState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IHouseRepository extends JpaRepository<House, Long> {
    List<House> findAllByAgent(Agent agent);
    List<House> findAllByCityState(CityState cityState);
    List<House> findAllByPriceIsLessThanEqual(Float price);
    List<House> findAllByPriceIsLessThanEqualAndCityState(Float price, CityState cityState);
}
