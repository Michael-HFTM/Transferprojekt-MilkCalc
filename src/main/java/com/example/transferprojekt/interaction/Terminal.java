package com.example.transferprojekt.interaction;

import com.example.transferprojekt.dataclasses.Address;
import com.example.transferprojekt.dataclasses.Company;
import com.example.transferprojekt.jpa.entities.SupplierEntity;
import com.example.transferprojekt.services.AdminToolsService;
import com.example.transferprojekt.services.SupplierService;
import com.example.transferprojekt.services.TestdataService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

@Component
public class Terminal {

    private final AdminToolsService adminToolsService;
    private final SupplierService supplierService;
    private final TestdataService testdataService;

    public Terminal(AdminToolsService adminToolsService, SupplierService supplierService, TestdataService testdataService) {
        this.adminToolsService = adminToolsService;
        this.supplierService = supplierService;
        this.testdataService = testdataService;
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
                        printAddSupplier(input);
                        break;

                    case 2:
                        printCompanies();
                        break;

                    case 3:
                        printDeleteSupplier(input);
                        break;

                    case 4:
                        printInsertTestdata();
                        break;

                    case 5:
                        printFlushAllDataTables(input);
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

    private void printSelection(){

        System.out.println("=================");
        System.out.println("Select operation:");
        System.out.println("1. Add supplier");
        System.out.println("2. List suppliers");
        System.out.println("3. Delete a supplier (by UUID)");
        System.out.println("4. TODO: Insert test data");
        System.out.println("5. Flush all data tables");
        System.out.println("0. Exit");
        System.out.print("Selection: ");

    }

    private void printAddSupplier(Scanner input) {

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
                SupplierEntity entity = supplierService.saveCompany(company);
                company = supplierService.mapToCompanyDataclass(entity);
                System.out.println("Supplier saved: " + company.toString());
                sucess = true;

            }  catch (Exception ex) {
                System.out.println(ex.getMessage());
                System.out.println("Invalid input, try again.");
            }
        }
    }

    private void printCompanies(){
        List<Company> companies = supplierService.getDatabaseEntries();
        for (Company company : companies) {
            System.out.println(company.toString());
        }
    }

    private void printDeleteSupplier(Scanner input){

        System.out.println("Delete supplier");
        System.out.print("Enter UUID: ");
        String inputUuid = input.nextLine();

        SupplierEntity entity;
        try {
            UUID uuid = UUID.fromString(inputUuid);
            entity = supplierService.getSupplierById(uuid);
            if (entity == null){
                System.out.println("Invalid UUID, aborting.");
                return;
            }

        } catch (Exception ex) {
            System.out.println("Encountered an issue:");
            System.out.println(ex.getMessage());
            System.out.println("Aborting.");
            return;
        }

        Company company = supplierService.mapToCompanyDataclass(entity);
        System.out.println(company.toString());
        System.out.println("Are you sure you want to delete this supplier? (y/n)");
        String choice = input.nextLine();

        if (choice.equalsIgnoreCase("y")) {
           if (supplierService.deleteSupplierById(entity.getSupplierId())) System.out.println("Supplier deleted.");

        } else {
            System.out.println("Aborting");
        }
    }

    private void printInsertTestdata(){
        testdataService.insertTestdata();
    }

    private void printFlushAllDataTables(Scanner input){

        System.out.println("Confirm deltion of all table data.");
        System.out.print("Type 'DELETE': ");
        String key = input.nextLine();
        adminToolsService.flushAllTables(key);
    }


}
