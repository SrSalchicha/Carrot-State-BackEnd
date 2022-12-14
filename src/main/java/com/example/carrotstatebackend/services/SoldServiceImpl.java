package com.example.carrotstatebackend.services;

import com.example.carrotstatebackend.controllers.dtos.request.CreateSoldRequest;
import com.example.carrotstatebackend.controllers.dtos.response.*;
import com.example.carrotstatebackend.controllers.dtos.response.persons.GetAgentResponse;
import com.example.carrotstatebackend.controllers.dtos.response.persons.GetClientResponse;
import com.example.carrotstatebackend.controllers.dtos.response.persons.GetRealStateResponse;
import com.example.carrotstatebackend.controllers.dtos.response.properties.GetHouseResponse;
import com.example.carrotstatebackend.controllers.dtos.response.properties.GetPlotResponse;
import com.example.carrotstatebackend.controllers.dtos.response.properties.GetPremiseResponse;
import com.example.carrotstatebackend.entities.*;
import com.example.carrotstatebackend.repositories.ISoldRepository;
import com.example.carrotstatebackend.services.interfaces.persons.IAgentService;
import com.example.carrotstatebackend.services.interfaces.ISoldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SoldServiceImpl implements ISoldService {

    @Autowired
    ISoldRepository repository;

    @Autowired
    IAgentService agentService;

    @Override
    public BaseResponse list() {
        List<GetSoldResponse> list = repository
                .findAll()
                .stream()
                .map(this::from)
                .collect(Collectors.toList());
        return BaseResponse.builder()
                .data(list)
                .message("All sold")
                .success(true)
                .httpStatus(HttpStatus.FOUND).build();
    }

    @Override
    public void create(CreateSoldRequest request) {
        repository.save(from(request));
    }

    private Sold from(CreateSoldRequest request){
        Date date = new Date();
        Sold sold = new Sold();
        sold.setAgent(request.getAgent());
        sold.setClient(request.getClient());
        sold.setDate(date);
        if (request.getHouse() != null){
            sold.setHouse(request.getHouse());
            float commission = request.getAgent().getRealState().getCommissionAgent();
            float price = request.getHouse().getPrice();
            sold.setComission((commission * price) / 100);
        }
        if (request.getPlot() != null){
            sold.setPlot(request.getPlot());
            float commission = request.getAgent().getRealState().getCommissionAgent();
            float price = request.getPlot().getPrice();
            sold.setComission((commission * price) / 100);
        }
        if (request.getPremise() != null){
            sold.setPremise(request.getPremise());
            float commission = request.getAgent().getRealState().getCommissionAgent();
            float price = request.getPlot().getPrice();
            sold.setComission((commission * price) / 100);
        }
        Agent agent = sold.getAgent();
        agent.setNumberOfSales(agent.getNumberOfSales() + 1);
        agentService.update(agent);
        return sold;
    }

    private GetSoldResponse from(Sold sold){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String date = formatter.format(sold.getDate());
        return GetSoldResponse.builder()
                .id(sold.getId())
                .date(date)
                .commission(sold.getComission())
                .agent(from(sold.getAgent()))
                .client(from(sold.getClient()))
                .house(from(sold.getHouse()))
                .plot(from(sold.getPlot()))
                .premise(from(sold.getPremise())).build();
    }

    private GetClientResponse from(Client client){
        return GetClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .contact(client.getContact())
                .budget(client.getBudget()).build();
    }

    private GetAgentResponse from(Agent agent){
        return GetAgentResponse.builder()
                .id(agent.getId())
                .name(agent.getName())
                .state(agent.getState())
                .email(agent.getEmail())
                .profilePicture(agent.getProfilePicture())
                .numberOfProperties(agent.getNumberOfProperties())
                .numberOfSales(agent.getNumberOfSales()).build();
    }

    private GetRealStateResponse from(RealState realState){
        GetRealStateResponse response = new GetRealStateResponse();
        response.setId(realState.getId());
        response.setName(realState.getName());
        response.setEmail(realState.getEmail());
        response.setCommissionAgent(realState.getCommissionAgent());
        response.setRealStateCode(realState.getCode().getCode());
        return response;
    }

    private GetHouseResponse from(House house){
        if (house == null) return null;
        GetHouseResponse response = new GetHouseResponse();
        response.setId(house.getId());
        response.setDescription(house.getDescription());
        response.setBathroomNum(house.getBathRoomNum());
        response.setPrice(house.getPrice());
        response.setLocation(house.getLocation());
        response.setFloors(house.getFloors());
        response.setRooms(house.getRooms());
        response.setSoldOut(house.getSoldOut());
        return response;
    }

    private GetPlotResponse from(Plot plot){
        if (plot == null) return null;
        GetPlotResponse response = new GetPlotResponse();
        response.setId(plot.getId());
        response.setDescription(plot.getDescription());
        response.setLocation(plot.getLocation());
        response.setPrice(plot.getPrice());
        response.setSize(plot.getSize());
        response.setName(plot.getName());
        response.setSoldOut(plot.getSoldOut());
        return response;
    }

    private GetPremiseResponse from(Premise premise){
        if (premise == null) return null;
        GetPremiseResponse response = new GetPremiseResponse();
        response.setId(premise.getId());
        response.setDescription(premise.getDescription());
        response.setLocation(premise.getLocation());
        response.setName(premise.getName());
        response.setPrice(premise.getPrice());
        response.setSize(premise.getSize());
        return response;
    }

}
