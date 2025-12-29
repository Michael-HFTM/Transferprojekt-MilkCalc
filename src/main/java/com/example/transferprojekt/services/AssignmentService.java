package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.jpa.entities.AssignmentEntity;
import com.example.transferprojekt.jpa.entities.SupplierNrEntity;
import com.example.transferprojekt.jpa.repositories.AssignmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SupplierService supplierService;
    private final SupplierNrService supplierNrService;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             SupplierService supplierService,
                             SupplierNrService supplierNrService) {
        this.assignmentRepository = assignmentRepository;
        this.supplierService = supplierService;
        this.supplierNrService = supplierNrService;
    }

    /**
     * Saves an assignment (CREATE or UPDATE)
     * If assignment has an ID, it updates; otherwise creates new
     */
    public AssignmentEntity save(Assignment assignment) {
        AssignmentEntity entity;

        // Check if this is an update (has UUID) or create (no UUID)
        if (assignment.getAssignmentId() != null) {
            // UPDATE: Load existing entity
            entity = assignmentRepository.findById(assignment.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Assignment not found for id: " + assignment.getAssignmentId()));

            // Update fields
            entity.setAssignmentStartDate(assignment.getValidFrom());
            entity.setAssignmentEndDate(assignment.getValidTo());
            entity.setSupplierEntity(supplierService.getEntityById(assignment.getSupplierId()));

            // Update supplier number
            SupplierNumber supplierNumber = assignment.getSupplierNumber();
            if (supplierNrService.exists(supplierNumber.getId())) {
                SupplierNrEntity supplierNrEntity = supplierNrService.getEntityById(supplierNumber.getId());
                entity.setSupplierNr(supplierNrEntity);
            } else {
                entity.setSupplierNr(supplierNrService.save(supplierNumber));
            }

        } else {
            // CREATE: New entity
            entity = mapToEntity(assignment);
        }

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
            /* fetch existing supplierNr from DB */
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

        return new Assignment(assignmentId, supplierId, supplierNumber, startDate, endDate);
    }

    public List<Assignment> getDatabaseEntries(){
        List<AssignmentEntity> entities = assignmentRepository.findAll();
        return entities.stream()
                .map(this::mapToDataclass)
                .toList();
    }

    public AssignmentEntity getById(UUID assignmentId){
        return assignmentRepository.findById(assignmentId).orElse(null);
    }

    /**
     * Deletes an assignment by ID
     */
    public boolean deleteById(UUID assignmentId) {
        try {
            assignmentRepository.deleteById(assignmentId);
            return true;
        } catch (Exception ex) {
            System.out.println("Exception while deleting assignment:");
            System.out.println(ex.getMessage());
            return false;
        }
    }
}