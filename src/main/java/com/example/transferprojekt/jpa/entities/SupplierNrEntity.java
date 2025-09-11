package com.example.transferprojekt.jpa.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "lieferantennummer")
public class SupplierNrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lieferant_nr", updatable = false, nullable = false)
    private int supplierNr;
}
