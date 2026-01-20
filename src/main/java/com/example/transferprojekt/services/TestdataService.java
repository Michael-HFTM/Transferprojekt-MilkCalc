package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.*;
import com.example.transferprojekt.enumerations.TimeWindow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TestdataService {

    private final AssignmentService assignmentService;
    private final MilkDeliveryService milkDeliveryService;
    private final SupplierService supplierService;
    private final SupplierNrService supplierNrService;

    public TestdataService(AssignmentService assignmentService, SupplierService supplierService, SupplierNrService supplierNrService, MilkDeliveryService milkDeliveryService) {
        this.assignmentService = assignmentService;
        this.supplierService = supplierService;
        this.supplierNrService = supplierNrService;
        this.milkDeliveryService = milkDeliveryService;
    }

    public void insertTestdata() {

        /* Fetch some of the default suppliernumbers from DB */
        SupplierNumber snr1 = supplierNrService.getById(1);
        SupplierNumber snr2 = supplierNrService.getById(2);
        SupplierNumber snr3 = supplierNrService.getById(3);


        /* Creating companies */
        Company comp1 = new Company("mueller@hof.ch", new Address("Hof Müller","Dorfstrasse 12","Musterdorf","9999"));
        Company comp2 = new Company("huber@biofarm.ch", new Address("Biofarm Huber","Landweg 5","Musterberg","9998"));
        Company comp3 = new Company("kontakt@alpenmilch.ch", new Address("Alpenmilch AG","Bergstrasse 7","Musterdorf","9999"));
        ArrayList<Company> companies = new ArrayList<>(List.of(comp1, comp2, comp3));
        System.out.println();
        System.out.println("Newly created Dataclasses:");
        printAllToString(companies);
        System.out.println("Saving to Database...");
        companies.replaceAll(company -> supplierService.mapToDataclass(supplierService.save(company)));
        System.out.println("Updated Dataclasses:");
        printAllToString(companies);


        /* Creating assignments */
        Assignment as1 = new Assignment(companies.get(0).getCompanyId(),snr1,LocalDate.parse("2025-01-01"));
        Assignment as2 = new Assignment(companies.get(1).getCompanyId(),snr2,LocalDate.parse("2025-01-01"));
        Assignment as3 = new Assignment(companies.get(2).getCompanyId(),snr3,LocalDate.parse("2025-01-01"),LocalDate.parse("2026-12-31"));
        ArrayList<Assignment> assignments = new ArrayList<>(List.of(as1,as2,as3));
        System.out.println();
        System.out.println("Newly created Dataclasses:");
        printAllToString(assignments);
        System.out.println("Saving to Database...");
        assignments.replaceAll(assignment -> assignmentService.mapToDataclass(assignmentService.save(assignment)));
        System.out.println("Updated Dataclasses:");
        printAllToString(assignments);


        /* Creating deliveries */
        MilkDelivery md1 = new MilkDelivery(new BigDecimal("150.50"),  LocalDate.parse("2026-01-01"), snr1 , TimeWindow.MORGEN );
        MilkDelivery md2 = new MilkDelivery(new BigDecimal("140.20"),  LocalDate.parse("2026-01-01"), snr1 , TimeWindow.ABEND);
        MilkDelivery md3 = new MilkDelivery(new BigDecimal("200.00"),  LocalDate.parse("2026-01-01"), snr2 , TimeWindow.MORGEN );
        MilkDelivery md4 = new MilkDelivery(new BigDecimal("105.75"),  LocalDate.parse("2026-01-01"), snr3 , TimeWindow.MORGEN );
        MilkDelivery md5 = new MilkDelivery(new BigDecimal("103.20"),  LocalDate.parse("2026-01-01"), snr3 , TimeWindow.ABEND );
        ArrayList<MilkDelivery> milkDeliveries = new ArrayList<>(List.of(md1, md2, md3, md4,md5));
        System.out.println();
        System.out.println("Newly created Dataclasses:");
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
