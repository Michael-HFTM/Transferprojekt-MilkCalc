package com.example.transferprojekt.jpa.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Lieferantennummer")
public class SupplierNrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LieferantNr", updatable = false, nullable = false)
    private int supplierNr;
}
