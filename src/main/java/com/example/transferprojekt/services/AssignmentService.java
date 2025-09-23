package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.jpa.entities.AssignmentEntity;
import com.example.transferprojekt.jpa.entities.SupplierNrEntity;
import com.example.transferprojekt.jpa.repositories.AssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SupplierService supplierService;
    private final SupplierNrService supplierNrService;

    public AssignmentService(AssignmentRepository assignmentRepository, SupplierService supplierService, SupplierNrService supplierNrService) {
        this.assignmentRepository = assignmentRepository;
        this.supplierService = supplierService;
        this.supplierNrService = supplierNrService;
    }

    public AssignmentEntity save(Assignment assignment) {
        AssignmentEntity entity = mapToEntity(assignment);
        return assignmentRepository.save(entity);
    }

    public AssignmentEntity mapToEntity(Assignment assignment) {
        AssignmentEntity entity = new AssignmentEntity();
        entity.setAssignmentId(assignment.getAssignmentId());
        entity.setAssignmentStartDate(assignment.getValidFrom());
        entity.setAssignmentEndDate(assignment.getValidTo());
        entity.setSupplierEntity(supplierService.getEntityById(assignment.getSupplierId()));

        SupplierNumber supplierNumber = assignment.getSupplierNumber();
        if (supplierNrService.exists(supplierNumber.getId())) {
            /* fetch exisiting supplierNr from DB */
            SupplierNrEntity supplierNrEntity = supplierNrService.getEntityById(supplierNumber.getId());
            entity.setSupplierNr(supplierNrEntity);
        } else {
            /* create new supplierNr in DB */
            entity.setSupplierNr(supplierNrService.save(supplierNumber));
        }



        return entity;
    }

    public Assignment mapToDataclass(AssignmentEntity entity) {
        UUID assignmentId = entity.getAssignmentId();
        UUID supplierId = entity.getSupplierEntity().getSupplierId();
        LocalDate startDate = entity.getAssignmentStartDate();
        LocalDate endDate = entity.getAssignmentEndDate();
        SupplierNumber supplierNumber = supplierNrService.mapToDataclass(entity.getSupplierNr());

        return new Assignment(assignmentId, supplierId,supplierNumber,startDate,endDate);
    }

    public List<Assignment> getDatabaseEntries(){
        List<AssignmentEntity> entities = assignmentRepository.findAll();
        return entities.stream()
                .map(this::mapToDataclass)
                .toList();
    }

    public AssignmentEntity getById(UUID deliveryId){
        return assignmentRepository.findById(deliveryId).orElse(null);
    }
}
