package com.example.transferprojekt;

import com.example.transferprojekt.dataclasses.Address;
import com.example.transferprojekt.dataclasses.Supplier;
import com.example.transferprojekt.dataclasses.SupplierNumber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransferprojektApplication {

    public static void main(String[] args) {

        //SpringApplication.run(TransferprojektApplication.class, args);

        System.out.println("TransferprojektApplication started");

        Supplier sup = new Supplier("test@mail.com", new Address("9999", "Musterweg 1", "Gasser", "Michael", "Musterdorf"), new SupplierNumber(1));

        System.out.println(sup);
        System.out.println(" ");
        System.out.println(sup.getAddress().toString());

    }

}
