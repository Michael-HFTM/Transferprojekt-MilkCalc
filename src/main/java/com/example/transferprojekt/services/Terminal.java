package com.example.transferprojekt.services;

import java.util.Scanner;

public class Terminal {

    public void startTerminal() {

        int selection = 99;
        Scanner input = new Scanner(System.in);

        do {
            printSelection();

            try {

                selection = input.nextInt();
                switch (selection) {

                    case 1:
                        System.out.println("1");
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
        System.out.println("0. Exit");
        System.out.print("Selection: ");

    }

}
