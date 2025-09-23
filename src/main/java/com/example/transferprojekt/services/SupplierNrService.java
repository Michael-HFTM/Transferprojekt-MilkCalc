package com.example.transferprojekt.services;

import com.example.transferprojekt.jpa.entities.SupplierNrEntity;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.jpa.repositories.SupplierNrRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierNrService {

    private final SupplierNrRepository supplierNrRepository;

    public SupplierNrService(SupplierNrRepository supplierNrRepository) {
        this.supplierNrRepository = supplierNrRepository;
    }

    public SupplierNrEntity save(SupplierNumber dataclass) {
        SupplierNrEntity entity = mapToEntity(dataclass);
        return supplierNrRepository.save(entity);
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

    public SupplierNumber getById(int id) {
        return supplierNrRepository.findById(id)
                .map(this::mapToDataclass)
                .orElseThrow(() -> new EntityNotFoundException("SupplierNrEntity not found for id: " + id));
    }

    public boolean exists(int id) {
        return supplierNrRepository.existsBySupplierNr(id);
    }

    public List<SupplierNumber> getDatabaseEntries(){
        List<SupplierNrEntity> entities = supplierNrRepository.findAll();
        return entities.stream()
                .map(this::mapToDataclass)
                .toList();
    }

}
