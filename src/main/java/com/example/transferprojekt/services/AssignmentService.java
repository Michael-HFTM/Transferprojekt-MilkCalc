package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.Assignment;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.jpa.entities.AssignmentEntity;
import com.example.transferprojekt.jpa.entities.SupplierNrEntity;
import com.example.transferprojekt.jpa.repositories.AssignmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
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
     * Finds the active assignment for a given supplier number and date
     *
     * @param supplierNumberId The supplier number to check
     * @param date The date for which the assignment should be active
     * @return The active assignment or null if none found
     */
    public Assignment getActiveAssignment(int supplierNumberId, LocalDate date) {
        List<AssignmentEntity> overlapping = assignmentRepository.findOverlappingAssignments(
                supplierNumberId, date, date, null);

        if (overlapping.isEmpty()) {
            return null;
        }

        // Return the first one found (there should only be one due to overlap checks during save)
        return mapToDataclass(overlapping.get(0));
    }

    /**
     * Gets a map of supplier number IDs to supplier names for active assignments on a specific date.
     *
     * @param date The date to check for active assignments
     * @return A map where key is supplier number ID and value is the supplier name
     */
    public java.util.Map<Integer, String> getActiveSupplierNames(LocalDate date) {
        List<Assignment> allAssignments = getDatabaseEntries();
        return allAssignments.stream()
                .filter(a -> !a.getValidFrom().isAfter(date) &&
                        (a.getValidTo() == null || !a.getValidTo().isBefore(date)))
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getSupplierNumber().getId(),
                        Assignment::getSupplierName,
                        (existing, replacement) -> existing // Keep the first one in case of overlaps (shouldn't happen)
                ));
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
        List<AssignmentEntity> overlapping = assignmentRepository.findOverlappingAssignments(
                supplierNumberId, validFrom, validTo, excludeAssignmentId);
        return !overlapping.isEmpty();
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
        String supplierName = entity.getSupplierEntity().getName();
        LocalDate startDate = entity.getAssignmentStartDate();
        LocalDate endDate = entity.getAssignmentEndDate();
        SupplierNumber supplierNumber = supplierNrService.mapToDataclass(entity.getSupplierNr());

        return new Assignment(assignmentId, supplierId, supplierName, supplierNumber, startDate, endDate);
    }

    public List<Assignment> getDatabaseEntries(){
        List<AssignmentEntity> entities = assignmentRepository.findAllWithSuppliers();
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