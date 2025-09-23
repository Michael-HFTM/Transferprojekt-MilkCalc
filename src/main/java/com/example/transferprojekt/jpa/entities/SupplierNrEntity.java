package com.example.transferprojekt.jpa.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "lieferantennummer")
public class SupplierNrEntity {

    @Id
    @Column(name = "lieferant_nr", updatable = false, nullable = false)
    private int supplierNr;

    public int getSupplierNr() {
        return supplierNr;
    }

    public void setSupplierNr(int supplierNr) {
        this.supplierNr = supplierNr;
    }
}
