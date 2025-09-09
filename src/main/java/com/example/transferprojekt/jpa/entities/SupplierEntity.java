package com.example.transferprojekt.jpa.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity
@Table(name = "Lieferant")
public class SupplierEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "LieferantID", updatable = false, nullable = false)
    private UUID supplierId;

    @Column(name = "Name", length = 50)
    private String name;

    @Column(name = "Adresse", length = 50)
    private String address;

    @Column(name = "PLZ", length = 4)
    private String zip;

    @Column(name = "Ort", length = 50)
    private String city;

    @Column(name = "Mail", length = 50)
    private String email;
}
