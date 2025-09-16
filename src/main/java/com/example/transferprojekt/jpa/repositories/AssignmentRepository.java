package com.example.transferprojekt.jpa.repositories;

import com.example.transferprojekt.dataclasses.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

}