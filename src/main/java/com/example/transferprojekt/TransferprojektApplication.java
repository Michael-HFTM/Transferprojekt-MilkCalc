package com.example.transferprojekt;

import com.example.transferprojekt.dataclasses.*;
import com.example.transferprojekt.interaction.Terminal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TransferprojektApplication {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(TransferprojektApplication.class, args);
        Terminal terminal = context.getBean(Terminal.class);
        terminal.startTerminal();
    }
}