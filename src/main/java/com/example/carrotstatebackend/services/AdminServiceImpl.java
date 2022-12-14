package com.example.carrotstatebackend.services;

import com.example.carrotstatebackend.controllers.dtos.response.BaseResponse;
import com.example.carrotstatebackend.controllers.dtos.response.persons.GetAdminResponse;
import com.example.carrotstatebackend.exceptions.NotFoundException;
import com.example.carrotstatebackend.entities.Admin;
import com.example.carrotstatebackend.repositories.persons.IAdminRepository;
import com.example.carrotstatebackend.services.interfaces.persons.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements IAdminService {

    @Autowired
    private IAdminRepository repository;

    @Override
    public BaseResponse getById(Integer id) {
        return BaseResponse.builder()
                .data(from(repository.findById(id).orElseThrow(NotFoundException::new)))
                .message("Admin: " + id)
                .success(true)
                .httpStatus(HttpStatus.FOUND).build();
    }

    @Override
    public Admin getAdmin(Integer id) {
        return repository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Override
    public Admin getAdmin(String email) {
        return repository.findByEmail(email).orElseThrow(NotFoundException::new);
    }

    private GetAdminResponse from(Admin admin){
        return GetAdminResponse.builder()
                .id(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail()).build();
    }
}
