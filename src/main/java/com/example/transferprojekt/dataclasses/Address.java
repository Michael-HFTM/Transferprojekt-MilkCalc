package com.example.transferprojekt.dataclasses;

public class Address {

    private String city, firstName, lastName, street, zip;

    public Address(String zip, String street, String lastName, String firstName, String city) {
        this.zip = zip;
        this.street = street;
        this.lastName = lastName;
        this.firstName = firstName;
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public String toString() {
        return "Address{" +
                "city='" + city + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", street='" + street + '\'' +
                ", zip='" + zip + '\'' +
                '}';
    }
}
