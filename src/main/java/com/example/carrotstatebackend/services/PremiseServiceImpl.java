package com.example.carrotstatebackend.services;

import com.example.carrotstatebackend.controllers.dtos.request.CreatePremiseRequest;
import com.example.carrotstatebackend.controllers.dtos.request.UpdatePremiseRequest;
import com.example.carrotstatebackend.controllers.dtos.response.BaseResponse;
import com.example.carrotstatebackend.controllers.dtos.response.GetHouseResponse;
import com.example.carrotstatebackend.controllers.dtos.response.GetPlotResponse;
import com.example.carrotstatebackend.controllers.dtos.response.GetPremiseResponse;
import com.example.carrotstatebackend.controllers.exceptions.NotFoundException;
import com.example.carrotstatebackend.entities.Agent;
import com.example.carrotstatebackend.entities.Owner;
import com.example.carrotstatebackend.entities.Plot;
import com.example.carrotstatebackend.entities.Premise;
import com.example.carrotstatebackend.repositories.IPremiseRepository;
import com.example.carrotstatebackend.services.interfaces.IAgentService;
import com.example.carrotstatebackend.services.interfaces.IPremiseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PremiseServiceImpl implements IPremiseService {

    @Autowired
    private IPremiseRepository repository;

    @Autowired
    private IAgentService agentService;

    @Override
    public  BaseResponse listByAgent(Long idAgent){
        return BaseResponse.builder()
                .data(getList(idAgent))
                .message("List")
                .success(true)
                .httpStatus(HttpStatus.FOUND)
                .build();
    }


    @Override
    public BaseResponse get(Long id) {
        GetPremiseResponse response = from(id);
        return BaseResponse.builder()
                .data(response)
                .message("the premise was find")
                .success(true)
                .httpStatus(HttpStatus.FOUND).build();
    }

    @Override
    public void delete(Long id) {repository.deleteById(id);}

    @Override
    public GetPremiseResponse updateToSoldOut(Long idPlot, Owner owner){
        Premise premise = findOneAndEnsureExist(idPlot);
        premise.setOwner(owner);
        premise.setSoldOut(true);
        return from(repository.save(premise));
    }

    public Premise getPremise(Long id){
        return findOneAndEnsureExist(id);
    }

    @Override
    public BaseResponse create(CreatePremiseRequest request, Long idAgent) {
        Premise premise = from(request);
        Agent agent = agentService.getAgent(idAgent);
        premise.setAgent(agent);
        GetPremiseResponse response = from(repository.save(premise));
        return BaseResponse.builder()
                .data(response)
                .message("the plot was created")
                .success(true)
                .httpStatus(HttpStatus.CREATED).build();
    }


    @Override
    public GetPremiseResponse update(Long id, UpdatePremiseRequest request) {

        Premise premise = findOneAndEnsureExist(id);
        premise = update(premise, request);
        return from(premise);

    }

    private Premise findOneAndEnsureExist(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("The Premise does not exist"));
    }

    private GetPremiseResponse from(Premise premise){
        GetPremiseResponse response = new GetPremiseResponse();
       response.setId(premise.getId());
       response.setDescription(premise.getDescription());
       response.setLocation(premise.getLocation());
       response.setName(premise.getName());
       response.setPrice(premise.getPrice());
       response.setSize(premise.getSize());
        return response;
    }

    private  Premise update(Premise premise, UpdatePremiseRequest request){
        premise.setDescription(request.getDescription());
        premise.setPrice(request.getPrice());
        premise.setName(request.getName());
        premise.setLocation(request.getLocation());
        premise.setSize(request.getSize());
        return repository.save(premise);
    }

    private  Premise from(CreatePremiseRequest request){
        Premise premise = new Premise();
        premise.setDescription(request.getDescription());
        premise.setPrice(request.getPrice());
        premise.setName(request.getName());
        premise.setLocation(request.getLocation());
        premise.setSize(request.getSize());
        premise.setSoldOut(false);
        return premise;
    }

    private GetPremiseResponse from(Long idPremise){
        return repository.findById(idPremise).map(this::from).orElseThrow(NotFoundException::new);
    }

    private List<GetPremiseResponse> getList(Long idAgent){
        Agent agent = agentService.getAgent(idAgent);
        return repository
                .findAllByAgent(agent)
                .stream()
                .map(this::from)
                .collect(Collectors.toList());
    }
}
