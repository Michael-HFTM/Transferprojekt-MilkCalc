package com.example.transferprojekt.dataclasses;

import java.math.BigDecimal;

public class SupplierNumber {

    private int id;

    public SupplierNumber(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SupplierNumber{" +
                "id=" + id +
                '}';
    }
}
