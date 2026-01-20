package com.example.transferprojekt.jpa.repositories;

import com.example.transferprojekt.jpa.entities.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

    @Query("SELECT a FROM AssignmentEntity a JOIN FETCH a.supplierId JOIN FETCH a.supplierNr")
    List<AssignmentEntity> findAllWithSuppliers();
}