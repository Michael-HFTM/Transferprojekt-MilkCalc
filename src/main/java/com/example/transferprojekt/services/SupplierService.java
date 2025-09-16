package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.Address;
import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.dataclasses.Supplier;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.jpa.entities.SupplierEntity;
import com.example.transferprojekt.jpa.entities.SupplierNrEntity;
import com.example.transferprojekt.jpa.repositories.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public void saveCompany(Company company) {
        SupplierEntity entity = mapToEntity(company);
        supplierRepository.save(entity);
    }

    /* Mapping: Dataclass<Company> -> Entity */
    public SupplierEntity mapToEntity(Company company) {
        SupplierEntity entity = new SupplierEntity();
        entity.setName(company.getAddress().getName());
        entity.setAddress(company.getAddress().getStreet());
        entity.setZip(company.getAddress().getZip());
        entity.setCity(company.getAddress().getCity());
        entity.setEmail(company.getMail());
        // Bei Bedarf SupplierNr noch ergänzen
        return entity;
    }

    /* Mapping: Entity -> Dataclass<Company> */
    public Company mapToCompanyDataclass(SupplierEntity entity) {
        Address address = new Address(
                entity.getZip(),
                entity.getAddress(),
                entity.getName(),
                entity.getCity()
        );
        return new Company(entity.getEmail(), address);
    }

    /* Mapping: Entity -> Dataclass<Company> */
    public Supplier mapToSupplierDataclass(SupplierEntity supEntity, SupplierNrEntity supNrEntity) {
        Address address = new Address(
                supEntity.getZip(),
                supEntity.getAddress(),
                supEntity.getName(),
                supEntity.getCity()
        );
        //TODO supplierNumber handeln sobald AssignmentService impelemtiert.
        SupplierNumber supplierNumber = null;
        return new Supplier(supEntity.getEmail(), address, supplierNumber);
    }

    public List<Company> getCompanies(){

        List<SupplierEntity> entities = supplierRepository.findAll();
        return entities.stream()
                .map(this::mapToCompanyDataclass)
                .toList();
    }
}
