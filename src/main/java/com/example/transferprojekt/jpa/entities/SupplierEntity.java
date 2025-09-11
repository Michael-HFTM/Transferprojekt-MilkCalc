package com.example.transferprojekt.jpa.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity
@Table(name = "lieferant")
public class SupplierEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "lieferant_id", updatable = false, nullable = false)
    private UUID supplierId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "adresse", length = 50)
    private String address;

    @Column(name = "plz", columnDefinition = "bpchar(4)")
    private String zip;

    @Column(name = "ort", length = 50)
    private String city;

    @Column(name = "mail", length = 50)
    private String email;
}
