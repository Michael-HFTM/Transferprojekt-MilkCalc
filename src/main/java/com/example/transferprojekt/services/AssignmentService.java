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
     * Checks if there is an overlapping assignment for the given supplier number
     * in the specified time range (excluding the assignment with excludeId if editing)
     *
     * @param supplierNumberId The supplier number to check
     * @param validFrom Start date of the assignment
     * @param validTo End date of the assignment (can be null for open-ended)
     * @param excludeAssignmentId Assignment ID to exclude from check (for edit mode)
     * @return true if there is an overlapping assignment, false otherwise
     */
    public boolean hasOverlappingAssignment(int supplierNumberId, LocalDate validFrom, LocalDate validTo, UUID excludeAssignmentId) {
        List<AssignmentEntity> existingAssignments = assignmentRepository.findAll();

        return existingAssignments.stream()
                .filter(a -> a.getSupplierNr().getSupplierNr() == supplierNumberId)
                .filter(a -> excludeAssignmentId == null || !a.getAssignmentId().equals(excludeAssignmentId))
                .anyMatch(existing -> {
                    LocalDate existingFrom = existing.getAssignmentStartDate();
                    LocalDate existingTo = existing.getAssignmentEndDate();

                    // Check for overlap
                    // Case 1: New assignment has no end date
                    if (validTo == null) {
                        // Overlaps if existing has no end date, or existing end is after or equal to new start
                        return existingTo == null || !existingTo.isBefore(validFrom);
                    }

                    // Case 2: Existing assignment has no end date
                    if (existingTo == null) {
                        // Overlaps if new end is after or equal to existing start
                        return !validTo.isBefore(existingFrom);
                    }

                    // Case 3: Both have end dates - check for any overlap
                    // No overlap only if: new ends before existing starts OR new starts after existing ends
                    return !(validTo.isBefore(existingFrom) || validFrom.isAfter(existingTo));
                });
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