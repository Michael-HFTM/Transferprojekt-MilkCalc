package com.example.transferprojekt.dataclasses;

import java.util.UUID;

public class Company {

    private UUID companyId; // DB generated
    private String mail;
    private Address address;

    public Company(String mail, Address address) {
        this.mail = mail;
        this.address = address;
    }

    /* constructor for existing DB objects */
    public Company(String mail, Address address, UUID companyId) {
        this.mail = mail;
        this.address = address;
        this.companyId = companyId;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Company{" +
                "mail='" + mail + '\'' +
                ", address=" + address.toString() +
                '}';
    }
}
