package com.example.transferprojekt;

import com.example.transferprojekt.dataclasses.*;
import com.example.transferprojekt.enumerations.TimeWindow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootApplication
public class TransferprojektApplication {

    public static void main(String[] args) {

        /* start Springboot */
        if(false) {
            SpringApplication.run(TransferprojektApplication.class, args);
        }

        /* test outputs for dataclasses */
        if(true){ testDataclasses(); }

    }

    public static void testDataclasses(){

        System.out.println("------------------------------------------");
        System.out.println("Tests for Dataclasses:");

        SupplierNumber snr1 = new SupplierNumber(1);
        Supplier sup = new Supplier("test@mail.com", new Address("9999", "Musterweg 1", "Gasser", "Michael", "Musterdorf"), snr1);
        MilkDelivery md = new MilkDelivery(new BigDecimal("10.35"),  LocalDate.now(), snr1 , TimeWindow.MORGEN );
        Assignment as = new Assignment(snr1,LocalDate.parse("2025-08-01"),LocalDate.parse("2026-12-31"));

        System.out.println(snr1);
        System.out.println(sup);
        System.out.println(md);
        System.out.println(as);

        System.out.println("------------------------------------------");

    }
}