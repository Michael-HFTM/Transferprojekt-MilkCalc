package com.example.transferprojekt.services;

import com.example.transferprojekt.jpa.repositories.AssignmentRepository;
import com.example.transferprojekt.jpa.repositories.MilkDeliveryRepository;
import com.example.transferprojekt.jpa.repositories.SupplierNrRepository;
import com.example.transferprojekt.jpa.repositories.SupplierRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminToolsService {

    private final AssignmentRepository assignmentRepository;
    private final MilkDeliveryRepository milkDeliveryRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierNrRepository supplierNrRepository;

    public AdminToolsService(
            AssignmentRepository assignmentRepository,
            MilkDeliveryRepository milkDeliveryRepository,
            SupplierRepository supplierRepository,
            SupplierNrRepository supplierNrRepository) {
        this.assignmentRepository = assignmentRepository;
        this.milkDeliveryRepository = milkDeliveryRepository;
        this.supplierRepository = supplierRepository;
        this.supplierNrRepository = supplierNrRepository;
    }

    public void flushAllTables(String key){
        if (key.equals("DELETE")){
            assignmentRepository.deleteAll();
            milkDeliveryRepository.deleteAll();
            supplierRepository.deleteAll();
            supplierNrRepository.deleteAll();
            System.out.println("All tables flushed!");
        } else {
            System.out.println("Invalid key, abborting");
        }
    }

}
