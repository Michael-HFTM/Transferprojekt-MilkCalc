package com.example.transferprojekt.dataclasses;

import java.time.LocalDate;
import java.util.UUID;

public class Assignment {

    private UUID assignmentId, supplierId; // DB generated
    private SupplierNumber supplierNumber;
    private LocalDate validFrom, validTo;


    public Assignment(UUID supplierId, SupplierNumber supplierNumber, LocalDate validFrom, LocalDate validTo) {
        this.supplierId = supplierId;
        this.supplierNumber = supplierNumber;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Assignment(UUID supplierId, SupplierNumber supplierNumber, LocalDate validFrom) {
        this.supplierId = supplierId;
        this.supplierNumber = supplierNumber;
        this.validFrom = validFrom;
    }

    /* constructor for existing DB objects */
    public Assignment(UUID assignmentId, UUID supplierId, SupplierNumber supplierNumber, LocalDate validFrom, LocalDate validTo) {
        this.assignmentId = assignmentId;
        this.supplierId = supplierId;
        this.supplierNumber = supplierNumber;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    /* constructor for existing DB objects */
    public Assignment(UUID assignmentId, UUID supplierId, SupplierNumber supplierNumber, LocalDate validFrom) {
        this.assignmentId = assignmentId;
        this.supplierId = supplierId;
        this.supplierNumber = supplierNumber;
        this.validFrom = validFrom;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(UUID assignmentId) {
        this.assignmentId = assignmentId;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
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
                "UUID=" + assignmentId +
                ", supplierId=" + supplierId +
                ", supplierNumber=" + supplierNumber.toString() +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}
