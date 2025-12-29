package com.example.transferprojekt;

import com.example.transferprojekt.interaction.Terminal;
import com.example.transferprojekt.javafx.JavaFxInitializer;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TransferprojektApplication {

    public static void main(String[] args) {

        // Prüfe ob Terminal-Mode oder GUI-Mode
        boolean terminalMode = args.length > 0 && args[0].equals("--terminal");

        if (terminalMode) {
            // Starte im Terminal-Modus (alte Funktionalität)
            ApplicationContext context = org.springframework.boot.SpringApplication.run(TransferprojektApplication.class, args);
            Terminal terminal = context.getBean(Terminal.class);
            terminal.startTerminal();
        } else {
            // Starte mit JavaFX GUI
            Application.launch(JavaFxInitializer.class, args);
        }
    }
}