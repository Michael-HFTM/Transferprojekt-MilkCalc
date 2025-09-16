package com.example.transferprojekt.services;

import com.example.transferprojekt.jpa.repositories.AssignmentRepository;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }



}
