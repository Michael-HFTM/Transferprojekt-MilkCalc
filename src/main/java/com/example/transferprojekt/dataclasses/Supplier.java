package com.example.transferprojekt.dataclasses;

public class Supplier extends Company{

    private SupplierNumber supplierNumber;

    public Supplier(String mail, Address address, SupplierNumber supplierNumber) {
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
                "supplierNumber=" + supplierNumber +
                ", mail='" + super.getMail() + '\'' +
                ", address=" + getAddress().toString() +
                '}';
    }
}
