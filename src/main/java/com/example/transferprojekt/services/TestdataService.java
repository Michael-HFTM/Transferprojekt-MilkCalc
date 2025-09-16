package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.*;
import com.example.transferprojekt.enumerations.TimeWindow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class TestdataService {

    private final SupplierService supplierService;

    public TestdataService(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    public void insertTestdata() {

        /* Creating example objects for dataclasses */
        SupplierNumber snr1 = new SupplierNumber(1);
        SupplierNumber snr2 = new SupplierNumber(2);
        SupplierNumber snr3 = new SupplierNumber(3);
        Supplier sup1 = new Supplier("mueller@hof.ch", new Address("Hof Müller","Dorfstrasse 12","Musterdorf","9999"), snr1);
        Supplier sup2 = new Supplier("huber@biofarm.ch", new Address("Biofarm Huber","Landweg 5","Musterberg","9998"), snr2);
        Supplier sup3 = new Supplier("ontakt@alpenmilch.ch", new Address("Alpenmilch AG","Bergstrasse 7","Musterdorf","9999"), snr3);
        Assignment as1 = new Assignment(snr1,LocalDate.parse("2025-01-01"));
        Assignment as2 = new Assignment(snr1,LocalDate.parse("2025-01-01"));
        Assignment as3 = new Assignment(snr1,LocalDate.parse("2025-01-01"),LocalDate.parse("2026-12-31"));
        MilkDelivery md1 = new MilkDelivery(new BigDecimal("150.50"),  LocalDate.parse("2025-09-01"), snr1 , TimeWindow.MORGEN );
        MilkDelivery md2 = new MilkDelivery(new BigDecimal("130.00"),  LocalDate.parse("2025-09-01"), snr2 , TimeWindow.MORGEN );
        MilkDelivery md3 = new MilkDelivery(new BigDecimal("105.75"),  LocalDate.parse("2025-09-01"), snr3 , TimeWindow.MORGEN );
        MilkDelivery md4 = new MilkDelivery(new BigDecimal("103.20"),  LocalDate.parse("2025-09-01"), snr3 , TimeWindow.ABEND );

        //TODO insert data into DB

    }

}
