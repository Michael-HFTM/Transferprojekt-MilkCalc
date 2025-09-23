package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.Address;
import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.jpa.entities.SupplierEntity;
import com.example.transferprojekt.jpa.repositories.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public SupplierEntity save(Company company) {
        SupplierEntity entity = mapToEntity(company);
        return supplierRepository.save(entity);
    }

    /* Mapping: Dataclass<Company> -> Entity */
    private SupplierEntity mapToEntity(Company company) {
        SupplierEntity entity = new SupplierEntity();
        entity.setName(company.getAddress().getName());
        entity.setStreet(company.getAddress().getStreet());
        entity.setZip(company.getAddress().getZip());
        entity.setCity(company.getAddress().getCity());
        entity.setEmail(company.getMail());
        // Bei Bedarf SupplierNr noch ergänzen
        return entity;
    }

    /* Mapping: Entity -> Dataclass<Company> */
    public Company mapToDataclass(SupplierEntity entity) {
        UUID supplierId = entity.getSupplierId();
        String email = entity.getEmail();
        Address address = new Address(
                entity.getName(),
                entity.getStreet(),
                entity.getCity(),
                entity.getZip()
        );
        return new Company(supplierId , email, address);
    }

    public List<Company> getDatabaseEntries(){
        List<SupplierEntity> entities = supplierRepository.findAll();
        return entities.stream()
                .map(this::mapToDataclass)
                .toList();
    }

    public SupplierEntity getById(UUID supplierId){
        return supplierRepository.findById(supplierId).orElse(null);
    }

    public SupplierEntity getEntityById(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SupplierNrEntity not found for id: " + id));
    }

    public boolean deleteById(UUID supplierId){
        try {
            supplierRepository.deleteById(supplierId);
            return true;
        } catch (Exception ex) {
            System.out.println("Exception while deleting supplier:");
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
