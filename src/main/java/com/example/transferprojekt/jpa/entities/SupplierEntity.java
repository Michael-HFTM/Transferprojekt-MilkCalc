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
    private String street;

    @Column(name = "plz", columnDefinition = "bpchar(4)")
    private String zip;

    @Column(name = "ort", length = 50)
    private String city;

    @Column(name = "mail", length = 50)
    private String email;

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String address) {
        this.street = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
