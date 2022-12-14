package com.example.carrotstatebackend.services;

import com.example.carrotstatebackend.controllers.dtos.request.properties.BasePropertyRequest;
import com.example.carrotstatebackend.controllers.dtos.request.properties.BaseHouseRequest;
import com.example.carrotstatebackend.controllers.dtos.request.RequestFilters;
import com.example.carrotstatebackend.controllers.dtos.response.BaseResponse;
import com.example.carrotstatebackend.controllers.dtos.response.properties.GetHouseResponse;
import com.example.carrotstatebackend.controllers.dtos.response.GetImageResponse;
import com.example.carrotstatebackend.entities.enums.converters.CityStateConverter;
import com.example.carrotstatebackend.exceptions.InvalidDeleteException;
import com.example.carrotstatebackend.exceptions.NotFoundException;
import com.example.carrotstatebackend.exceptions.NotValidCityCodeException;
import com.example.carrotstatebackend.entities.Agent;
import com.example.carrotstatebackend.entities.Client;
import com.example.carrotstatebackend.entities.House;
import com.example.carrotstatebackend.entities.enums.CityState;
import com.example.carrotstatebackend.entities.pivots.ImageHouse;
import com.example.carrotstatebackend.repositories.properties.IHouseRepository;
import com.example.carrotstatebackend.services.evaluators.IFilterUtility;
import com.example.carrotstatebackend.services.interfaces.persons.IAgentService;
import com.example.carrotstatebackend.services.interfaces.pivtos.IBaseImageService;
import com.example.carrotstatebackend.services.interfaces.properties.IBasePropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HouseServiceImpl implements IBasePropertyService<House> {

    @Autowired
    private IHouseRepository repository;

    @Autowired
    private IAgentService agentService;

    @Autowired
    private CityStateConverter cityStateConverter;

    @Autowired
    @Qualifier("imgHouse")
    private IBaseImageService imageHouseService;

    @Autowired
    private IFilterUtility filtersUtility;

    @Override
    public BaseResponse list() {
        List<GetHouseResponse> list = repository
                .findAll()
                .stream()
                .map(this::from)
                .collect(Collectors.toList());
        return BaseResponse.builder()
                .data(list)
                .message("houses")
                .success(true)
                .httpStatus(HttpStatus.FOUND)
                .build();
    }

    @Override
    public BaseResponse listByAgent(Long idAgent) {
        return BaseResponse.builder()
                .data(getList(idAgent))
                .message("found")
                .success(true)
                .httpStatus(HttpStatus.FOUND).build();
    }

    @Override
    public BaseResponse get(Long id) {
        return BaseResponse.builder()
                .data(from(id))
                .message("found")
                .httpStatus(HttpStatus.FOUND).build();
    }

    @Override
    public BaseResponse search(String keyWord, RequestFilters filters) {
        if (filters.getUseKeyWord()){
            List<GetHouseResponse> finalList = filter(filters).stream()
                    .filter(getHouseResponse -> evaluate(getHouseResponse, keyWord))
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
        return BaseResponse.builder()
                .data(from((BaseHouseRequest) request, idAgent))
                .message("the house was created")
                .success(true)
                .httpStatus(HttpStatus.CREATED).build();
    }

    @Override
    public BaseResponse update(Long idHouse, BasePropertyRequest request) {
        House house = repository.findById(idHouse)
                .orElseThrow(NotFoundException::new);
        return BaseResponse.builder()
                .data(from(house, (BaseHouseRequest) request))
                .message("the house was updated")
                .success(true)
                .httpStatus(HttpStatus.ACCEPTED).build();
    }

    @Override
    public BaseResponse delete(Long id) {
        House house = repository.findById(id)
                .orElseThrow(NotFoundException::new);
        if (house.getClient() != null){
            throw new InvalidDeleteException();
        }
        repository.deleteById(id);
        return BaseResponse.builder()
                .message("the house was deleted")
                .httpStatus(HttpStatus.ACCEPTED)
                .success(true).build();
    }

    @Override
    public BaseResponse updateToSoldOut(Long idHouse, Client client) {
        House house = findOneAndEnsureExist(idHouse);
        house.setClient(client);
        house.setSoldOut(true);
        return BaseResponse.builder()
                .data(from(house))
                .message("updated")
                .success(true)
                .httpStatus(HttpStatus.ACCEPTED).build();
    }

    @Override
    public House getPropertyE(Long id){
        return findOneAndEnsureExist(id);
    }

    private House findOneAndEnsureExist(Long id) {
        return repository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    private GetHouseResponse from(BaseHouseRequest request, Long idAgent){
        House house = from(request);
        Agent agent = agentService.getAgent(idAgent);
        house.setAgent(agent);
        house = repository.save(house);
        agent.setNumberOfProperties(agent.getNumberOfProperties() + 1);
        agentService.update(agent);
        return from(house);
    }

    private GetHouseResponse from(House house){
        GetHouseResponse response = new GetHouseResponse();
        response.setName(house.getName());
        response.setId(house.getId());
        response.setBathroomNum(house.getBathRoomNum());
        response.setDescription(house.getDescription());
        response.setLocation(house.getLocation());
        response.setFloors(house.getFloors());
        response.setRooms(house.getRooms());
        response.setSoldOut(house.getSoldOut());
        response.setPrice(house.getPrice());
        response.setCityState(house.getCityState());
        if(house.getImageHouses() != null ) {
            response.setImages(from(house.getImageHouses()));
        }
        return response;
    }

    private GetHouseResponse from(House house, BaseHouseRequest request){
        house.setBathRoomNum(request.getBathRoomNum());
        house.setDescription(request.getDescription());
        house.setFloors(request.getFloors());
        house.setLocation(request.getLocation());
        house.setRooms(request.getRooms());
        house.setSize(request.getSize());
        house.setName(request.getName());
        house.setPrice(request.getPrice());
        return from(repository.save(house));
    }

    private  House from(BaseHouseRequest request){
        House house = new House();
        house.setName(request.getName());
        house.setBathRoomNum(request.getBathRoomNum());
        house.setDescription(request.getDescription());
        house.setSize(request.getSize());
        house.setPrice(request.getPrice());
        house.setFloors(request.getFloors());
        house.setLocation(request.getLocation());
        house.setCityState(from(request.getCityCode()));
        house.setRooms(request.getRooms());
        house.setSoldOut(false);
        return house;
    }

    private List<GetImageResponse> from(List<ImageHouse> image){
        return image.stream()
                .map(this::from)
                .collect(Collectors.toList());
    }

    private GetImageResponse from(ImageHouse imageHouse){
        return GetImageResponse.builder().url(imageHouse.getUrl()).build();
    }

    private CityState from(String cityCode){
        return cityStateConverter.convertToEntityAttribute(cityCode);
    }

    private GetHouseResponse from(Long idHouse){
        return repository.findById(idHouse)
                .map(this::from)
                .orElseThrow(NotFoundException::new);
    }

    private List<GetHouseResponse> getList(Long idAgent){
        Agent agent = agentService.getAgent(idAgent);
        return repository
                .findAllByAgent(agent)
                .stream()
                .map(this::from)
                .collect(Collectors.toList());
    }

    private List<GetHouseResponse> filter(RequestFilters filters) {
        switch (filtersUtility.filter(filters)){
            case BY_CITY_CODE:
                return repository.findAllByCityState(from(filters.getCityCode()))
                        .stream()
                        .map(this::from)
                        .collect(Collectors.toList());
            case BY_PRICE:
                return repository.findAllByPriceIsLessThanEqual(filters.getBudget())
                        .stream()
                        .map(this::from)
                        .collect(Collectors.toList());
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

    private Boolean evaluate(GetHouseResponse house, String keyWord){
        return house
                .getName()
                .contains(keyWord) || house.getDescription()
                .contains(keyWord);
    }
}

//base response,
