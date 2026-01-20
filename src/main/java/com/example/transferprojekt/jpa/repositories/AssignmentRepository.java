package com.example.transferprojekt.jpa.repositories;

import com.example.transferprojekt.jpa.entities.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

    @Query("SELECT a FROM AssignmentEntity a JOIN FETCH a.supplierId JOIN FETCH a.supplierNr")
    List<AssignmentEntity> findAllWithSuppliers();

    @Query("""
        SELECT a FROM AssignmentEntity a 
        WHERE a.supplierNr.supplierNr = :supplierNr 
        AND (:excludeId IS NULL OR a.assignmentId <> :excludeId)
        AND (
            (CAST(:validTo AS LocalDate) IS NULL AND (a.assignmentEndDate IS NULL OR a.assignmentEndDate >= :validFrom))
            OR (a.assignmentEndDate IS NULL AND :validTo >= a.assignmentStartDate)
            OR (CAST(:validTo AS LocalDate) IS NOT NULL AND a.assignmentEndDate IS NOT NULL AND NOT (:validTo < a.assignmentStartDate OR :validFrom > a.assignmentEndDate))
        )
    """)
    List<AssignmentEntity> findOverlappingAssignments(
            int supplierNr,
            java.time.LocalDate validFrom,
            java.time.LocalDate validTo,
            java.util.UUID excludeId
    );
}