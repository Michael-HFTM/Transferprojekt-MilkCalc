package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.*;
import com.example.transferprojekt.enumerations.TimeWindow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestdataService {

    private final SupplierService supplierService;
    private final SupplierNrService supplierNrService;
    private final MilkDeliveryService milkDeliveryService;

    public TestdataService(SupplierService supplierService, SupplierNrService supplierNrService, MilkDeliveryService milkDeliveryService) {
        this.supplierService = supplierService;
        this.supplierNrService = supplierNrService;
        this.milkDeliveryService = milkDeliveryService;
    }

    public void insertTestdata() {

        /* Fetch some of the default suppliernumbers from DB */
        SupplierNumber snr1 = supplierNrService.getById(1);
        SupplierNumber snr2 = supplierNrService.getById(2);
        SupplierNumber snr3 = supplierNrService.getById(3);

        /* Creating example objects for dataclasses */
        Supplier sup1 = new Supplier("mueller@hof.ch", new Address("Hof Müller","Dorfstrasse 12","Musterdorf","9999"), snr1);
        Supplier sup2 = new Supplier("huber@biofarm.ch", new Address("Biofarm Huber","Landweg 5","Musterberg","9998"), snr2);
        Supplier sup3 = new Supplier("ontakt@alpenmilch.ch", new Address("Alpenmilch AG","Bergstrasse 7","Musterdorf","9999"), snr3);
        ArrayList<Supplier> suppliers = new ArrayList<>(List.of(sup1, sup2, sup3));

        Assignment as1 = new Assignment(snr1,LocalDate.parse("2025-01-01"));
        Assignment as2 = new Assignment(snr1,LocalDate.parse("2025-01-01"));
        Assignment as3 = new Assignment(snr1,LocalDate.parse("2025-01-01"),LocalDate.parse("2026-12-31"));
        ArrayList<Assignment> assignments = new ArrayList<>(List.of(as1,as2,as3));

        MilkDelivery md1 = new MilkDelivery(new BigDecimal("150.50"),  LocalDate.parse("2025-09-01"), snr1 , TimeWindow.MORGEN );
        MilkDelivery md2 = new MilkDelivery(new BigDecimal("130.00"),  LocalDate.parse("2025-09-01"), snr2 , TimeWindow.MORGEN );
        MilkDelivery md3 = new MilkDelivery(new BigDecimal("105.75"),  LocalDate.parse("2025-09-01"), snr3 , TimeWindow.MORGEN );
        MilkDelivery md4 = new MilkDelivery(new BigDecimal("103.20"),  LocalDate.parse("2025-09-01"), snr3 , TimeWindow.ABEND );
        ArrayList<MilkDelivery> milkDeliveries = new ArrayList<>(List.of(md1, md2, md3, md4));

        //TODO insert data into DB

//        for (Supplier supplier : suppliers) {
//
//        }
//
//        for (Assignment assignment : assignments) {
//
//        }

        System.out.println("Newly created Dataclasses:");
        printAllToString(suppliers);
        printAllToString(assignments);
        printAllToString(milkDeliveries);

        System.out.println("Saving to Database...");
        milkDeliveries.replaceAll(milkDelivery -> milkDeliveryService.mapToDataclass(milkDeliveryService.save(milkDelivery)));

        System.out.println("Updated Dataclasses:");
        printAllToString(milkDeliveries);

    }

    public static <T> void printAllToString(List<T> list) {
        list.forEach(element -> System.out.println(element.toString()));
    }

}
