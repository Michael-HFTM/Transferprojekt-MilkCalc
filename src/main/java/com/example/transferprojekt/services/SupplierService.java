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
import java.util.UUID;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public SupplierEntity saveCompany(Company company) {
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
    public Company mapToCompanyDataclass(SupplierEntity entity) {
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

    /* Mapping: Entity -> Dataclass<Supplier> */
    /* prepped for future use */
    public Supplier mapToSupplierDataclass(SupplierEntity entity, SupplierNrEntity supNrEntity) {
        UUID supplierId = entity.getSupplierId();
        String email = entity.getEmail();
        Address address = new Address(
                entity.getName(),
                entity.getStreet(),
                entity.getCity(),
                entity.getZip()
        );
        //TODO supplierNumber handeln sobald AssignmentService impelemtiert.
        SupplierNumber supplierNumber = null;

        return new Supplier(supplierId, email, address, supplierNumber);
    }

    public List<Company> getDatabaseEntries(){
        List<SupplierEntity> entities = supplierRepository.findAll();
        return entities.stream()
                .map(this::mapToCompanyDataclass)
                .toList();
    }

    public SupplierEntity getSupplierById(UUID supplierId){
        return supplierRepository.findById(supplierId).orElse(null);
    }

    public boolean deleteSupplierById(UUID supplierId){
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
