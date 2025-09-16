package com.example.transferprojekt.services;

import com.example.transferprojekt.jpa.entities.SupplierNrEntity;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.jpa.repositories.SupplierNrRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SupplierNrService {

    private final SupplierNrRepository supplierNrRepository;

    public SupplierNrService(SupplierNrRepository supplierNrRepository) {
        this.supplierNrRepository = supplierNrRepository;
    }

    // Entity -> DTO
    private SupplierNumber mapToDataclass(SupplierNrEntity entity) {
        return new SupplierNumber(entity.getSupplierNr());
    }

    // DTO -> Entity
    private SupplierNrEntity mapToEntity(SupplierNumber dataclass) {
        SupplierNrEntity entity = new SupplierNrEntity();
        entity.setSupplierNr(dataclass.getId());
        return entity;
    }

//    public SupplierNumber getSupplierNumberById(int id) {
//        return supplierNrRepository.findById(id).orElse(null);
//    }

}
