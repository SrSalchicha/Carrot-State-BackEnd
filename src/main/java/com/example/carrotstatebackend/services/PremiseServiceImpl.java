package com.example.carrotstatebackend.services;

import com.example.carrotstatebackend.controllers.dtos.request.properties.BasePremiseRequest;
import com.example.carrotstatebackend.controllers.dtos.request.RequestFilters;
import com.example.carrotstatebackend.controllers.dtos.request.properties.BasePropertyRequest;
import com.example.carrotstatebackend.controllers.dtos.response.BaseResponse;
import com.example.carrotstatebackend.controllers.dtos.response.GetImageResponse;
import com.example.carrotstatebackend.controllers.dtos.response.properties.GetPremiseResponse;
import com.example.carrotstatebackend.exceptions.InvalidDeleteException;
import com.example.carrotstatebackend.exceptions.NotFoundException;
import com.example.carrotstatebackend.exceptions.NotValidCityCodeException;
import com.example.carrotstatebackend.entities.Agent;
import com.example.carrotstatebackend.entities.Client;
import com.example.carrotstatebackend.entities.Premise;
import com.example.carrotstatebackend.entities.enums.CityState;
import com.example.carrotstatebackend.entities.pivots.ImagePremise;
import com.example.carrotstatebackend.repositories.properties.IPremiseRepository;
import com.example.carrotstatebackend.services.evaluators.FiltersUtilityImpl;
import com.example.carrotstatebackend.services.evaluators.IFilterUtility;
import com.example.carrotstatebackend.services.interfaces.persons.IAgentService;
import com.example.carrotstatebackend.services.interfaces.properties.IBasePropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PremiseServiceImpl implements IBasePropertyService<Premise> {

    @Autowired
    private IPremiseRepository repository;

    @Autowired
    private IAgentService agentService;

    @Autowired
    private FiltersUtilityImpl filtersUtilityImpl;

    @Autowired
    private IFilterUtility filtersUtility;

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
    public BaseResponse list() {
        List<GetPremiseResponse> list = repository.findAll()
                .stream().map(this::from).collect(Collectors.toList());
        return BaseResponse.builder()
                .data(list)
                .success(true)
                .message("list")
                .httpStatus(HttpStatus.FOUND).build();
    }

    @Override
    public BaseResponse search(String keyWord, RequestFilters filters) {
        if (filters.getUseKeyWord()){
            List<GetPremiseResponse> finalList = filter(filters).stream()
                    .filter(getPremiseResponse -> evaluate(getPremiseResponse, keyWord))
                    .collect(Collectors.toList());
            BaseResponse.builder()
                    .data(finalList)
                    .message("filter")
                    .success(true)
                    .httpStatus(HttpStatus.FOUND)
                    .build();
        }
        return BaseResponse.builder()
                .data(filter(filters))
                .message("filter")
                .success(true)
                .httpStatus(HttpStatus.FOUND)
                .build();
    }

    @Override
    public BaseResponse create(BasePropertyRequest request, Long idAgent) {
        Premise premise = from((BasePremiseRequest) request);
        Agent agent = agentService.getAgent(idAgent);
        premise.setAgent(agent);
        GetPremiseResponse response = from(repository.save(premise));
        agent.setNumberOfProperties(agent.getNumberOfSales() + 1);
        agentService.update(agent);
        return BaseResponse.builder()
                .data(response)
                .message("the premise was created")
                .success(true)
                .httpStatus(HttpStatus.CREATED).build();
    }

    @Override
    public BaseResponse update(Long id, BasePropertyRequest request) {
        Premise premise = findOneAndEnsureExist(id);
        premise = update(premise, (BasePremiseRequest) request);
        return BaseResponse.builder()
                .data(from(premise))
                .message("the premise was uploaded")
                .success(true)
                .httpStatus(HttpStatus.ACCEPTED).build();
    }

    @Override
    public BaseResponse delete(Long id) {
        Premise premise = repository.findById(id).orElseThrow(NotFoundException::new);
        if (premise.getClient() != null) throw new InvalidDeleteException();
        repository.delete(premise);
        return BaseResponse.builder()
                .message("the premise was deleted")
                .success(true)
                .httpStatus(HttpStatus.ACCEPTED).build();
    }

    @Override
    public Premise getPropertyE(Long id){
        return findOneAndEnsureExist(id);
    }

    @Override
    public BaseResponse updateToSoldOut(Long idPlot, Client owner){
        Premise premise = findOneAndEnsureExist(idPlot);
        premise.setClient(owner);
        premise.setSoldOut(true);
        return BaseResponse.builder()
                .data(from(repository.save(premise)))
                .message("updated")
                .success(true)
                .httpStatus(HttpStatus.ACCEPTED).build();
    }

    private Premise findOneAndEnsureExist(Long id) {
        return repository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    private GetPremiseResponse from(Premise premise){
       GetPremiseResponse response = new GetPremiseResponse();
       response.setId(premise.getId());
       response.setDescription(premise.getDescription());
       response.setLocation(premise.getLocation());
       response.setName(premise.getName());
       response.setPrice(premise.getPrice());
       response.setSize(premise.getSize());
       response.setCityState(premise.getCityState());
       response.setImages(from(premise.getImagePremises()));
       return response;
    }

    private  Premise update(Premise premise, BasePremiseRequest request){
        premise.setDescription(request.getDescription());
        premise.setPrice(request.getPrice());
        premise.setName(request.getName());
        premise.setLocation(request.getLocation());
        premise.setSize(request.getSize());
        return repository.save(premise);
    }

    private  Premise from(BasePremiseRequest request){
        Premise premise = new Premise();
        premise.setDescription(request.getDescription());
        premise.setPrice(request.getPrice());
        premise.setName(request.getName());
        premise.setLocation(request.getLocation());
        premise.setSize(request.getSize());
        premise.setSoldOut(false);
        premise.setCityState(from(request.getCityCode()));
        return premise;
    }

    private List<GetImageResponse> from(List<ImagePremise> image){
        return image.stream()
                .map(imagePremise -> GetImageResponse.builder().url(imagePremise.getUrl()).build())
                .collect(Collectors.toList());
    }

    private CityState from(String cityCode){
        return Stream.of(CityState.values())
                .filter(c -> c.getLocationCode().equals(cityCode))
                .findFirst().orElseThrow(() -> new NotValidCityCodeException(cityCode));
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

    private List<GetPremiseResponse> filter(RequestFilters filters) {
        switch (filtersUtility.filter(filters)){
            case BY_CITY_CODE:
                return repository.findAllByCityState(from(filters.getCityCode()))
                        .stream()
                        .map(this::from)
                        .collect(Collectors.toList());
            case BY_PRICE:
                return repository.findAllByPriceIsLessThanEqual(filters.getBudget())
                        .stream().map(this::from).collect(Collectors.toList());
            case BY_ALL_FILTERS:
                return repository.findAllByPriceIsLessThanEqualAndCityState(
                                filters.getBudget(), from(filters.getCityCode()))
                        .stream()
                        .map(this::from)
                        .collect(Collectors.toList());
            default:
                throw new RuntimeException();
        }
    }

    private Boolean evaluate(GetPremiseResponse premise, String keyWord){
        return premise
                .getName()
                .contains(keyWord) || premise.getDescription().contains(keyWord);
    }


}
