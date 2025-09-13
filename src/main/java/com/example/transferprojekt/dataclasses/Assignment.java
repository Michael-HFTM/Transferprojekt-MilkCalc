package com.example.transferprojekt.dataclasses;

import java.time.LocalDate;

public class Assignment {

    private SupplierNumber supplierNumber;
    private LocalDate validFrom, validTo;

    public Assignment(SupplierNumber supplierNumber, LocalDate validFrom, LocalDate validTo) {
        this.supplierNumber = supplierNumber;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Assignment(SupplierNumber supplierNumber, LocalDate validFrom) {
        this.supplierNumber = supplierNumber;
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public SupplierNumber getSupplierNumber() {
        return supplierNumber;
    }

    public void setSupplierNumber(SupplierNumber supplierNumber) {
        this.supplierNumber = supplierNumber;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "supplierNumber=" + supplierNumber.toString() +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}
