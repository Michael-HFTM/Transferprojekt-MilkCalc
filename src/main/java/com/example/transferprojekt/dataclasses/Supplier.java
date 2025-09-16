package com.example.transferprojekt.dataclasses;

import java.util.UUID;

public class Supplier extends Company{

    private UUID supplierId;
    private SupplierNumber supplierNumber;

    public Supplier(String mail, Address address, SupplierNumber supplierNumber) {
        super(mail, address);
        this.supplierNumber = supplierNumber;
    }

    /* constructor for existing DB objects */
    public Supplier(UUID supplierId, String mail, Address address, SupplierNumber supplierNumber) {
        super(mail, address);
        this.supplierNumber = supplierNumber;
    }

    public SupplierNumber getSupplierNumber() {
        return supplierNumber;
    }

    public void setSupplierNumber(SupplierNumber supplierNumber) {
        this.supplierNumber = supplierNumber;
    }

    public void removeSupplierNumber(){
        this.supplierNumber = null;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "UUID=" + super.getCompanyId() +
                ", supplierNumber=" + supplierNumber +
                ", mail='" + super.getMail() + '\'' +
                ", address=" + getAddress().toString() +
                '}';
    }
}
