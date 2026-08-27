package com.example.eventhub.helper;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class HelpForService {

    public <T>T idCheck(Long id,JpaRepository<T,Long> repository,String entity){
        if (id<=0){
            throw new IllegalArgumentException("Id can not be <=0");
        }
        return repository.findById(id).orElseThrow(()-> new EntityNotFoundException(entity+" with id: "+id+" not found"));
    }
}
