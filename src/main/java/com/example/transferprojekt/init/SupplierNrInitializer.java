package com.example.transferprojekt.init;

import com.example.transferprojekt.dataclasses.SupplierNumber;
import com.example.transferprojekt.services.SupplierNrService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SupplierNrInitializer implements CommandLineRunner {
    private final SupplierNrService supplierNrService;

    public SupplierNrInitializer(SupplierNrService supplierNrService) {
        this.supplierNrService = supplierNrService;
    }

    @Override
    public void run(String... args) {

        List<SupplierNumber> defaultNumbers = new ArrayList<>();
        for (int i = 1; i <=10; i++){
            if ( !supplierNrService.exists(i)){
                defaultNumbers.add(new SupplierNumber(i));
            }
        }
        for (SupplierNumber supNr:defaultNumbers) {
            supplierNrService.save(supNr);
        }
    }
}
