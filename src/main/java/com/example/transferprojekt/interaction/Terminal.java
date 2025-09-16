package com.example.transferprojekt.interaction;

import com.example.transferprojekt.dataclasses.Address;
import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.services.SupplierService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class Terminal {

    private final SupplierService supplierService;

    public Terminal(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    public void startTerminal() {

        int selection = 99;
        Scanner input = new Scanner(System.in);

        // flush terminal
        for (int i = 0; i < 20; i++) {
            System.out.println();
        }

        do {
            printSelection();

            try {

                selection = input.nextInt();
                input.nextLine();
                switch (selection) {

                    case 1:
                        addSupplier(input);
                        break;

                    case 2:
                        printCompanies();
                        break;

                    case 0:
                        System.out.println("Shutting down");
                        break;

                    default:
                        System.out.println("Invalid selection, try again.");
                }

            } catch (Exception ex) {

                System.out.println("Invalid selection, try again.");
                input.nextLine();
            }

            System.out.println();
        } while (selection != 0);

        input.close();
    }

    public void printSelection(){

        System.out.println("=================");
        System.out.println("Select operation:");
        System.out.println("1. Add supplier");
        System.out.println("2. Print suppliers");
        System.out.println("0. Exit");
        System.out.print("Selection: ");

    }

    private void addSupplier(Scanner input) {

        boolean sucess = false;

        while (!sucess) {

            System.out.print("Enter name: ");
            String name = input.nextLine();

            System.out.print("Enter street: ");
            String street = input.nextLine();

            System.out.print("Enter city: ");
            String city = input.nextLine();

            System.out.print("Enter ZIP code: ");
            String zip = input.nextLine();

            System.out.print("Enter email: ");
            String email = input.nextLine();

            try {
                Address address = new Address(name, street, city, zip);
                Company company = new Company(email, address);
                supplierService.saveCompany(company);
                System.out.println("Supplier saved: " + company);
                sucess = true;

            }  catch (Exception ex) {
                System.out.println(ex.getMessage());
                System.out.println("Invalid input, try again.");
            }
        }
    }

    private void printCompanies(){
        List<Company> companies = supplierService.getCompanies();
        for (Company company : companies) {
            System.out.println(company.toString());
        }
    }

}
