package com.example.transferprojekt;

import com.example.transferprojekt.dataclasses.*;
import com.example.transferprojekt.enumerations.TimeWindow;
import com.example.transferprojekt.interaction.Terminal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootApplication
public class TransferprojektApplication {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(TransferprojektApplication.class, args);
        Terminal terminal = context.getBean(Terminal.class);
        terminal.startTerminal();

        /* test outputs for dataclasses */
        if(false){ testDataclasses(); }
    }

    public static void testDataclasses(){

        System.out.println("------------------------------------------");
        System.out.println("Tests for Dataclasses:");

        SupplierNumber snr1 = new SupplierNumber(1);
        Supplier sup = new Supplier("test@mail.com", new Address("Gasser Michael","Musterweg 1","Musterdorf","9999"), snr1);
        MilkDelivery md = new MilkDelivery(new BigDecimal("10.35"),  LocalDate.now(), snr1 , TimeWindow.MORGEN );
        Assignment as = new Assignment(snr1,LocalDate.parse("2025-08-01"),LocalDate.parse("2026-12-31"));

        System.out.println(snr1);
        System.out.println(sup);
        System.out.println(md);
        System.out.println(as);

        System.out.println("------------------------------------------");

    }
}