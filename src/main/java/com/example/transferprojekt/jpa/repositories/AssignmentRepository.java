package com.example.transferprojekt.jpa.repositories;

import com.example.transferprojekt.jpa.entities.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

}