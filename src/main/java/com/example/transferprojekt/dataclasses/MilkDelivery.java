package com.example.transferprojekt.dataclasses;

import com.example.transferprojekt.enumerations.TimeWindow;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MilkDelivery {

    private BigDecimal amountKg;
    private LocalDate date;
    private SupplierNumber supplierNumber;
    private TimeWindow timeWindow;

    public MilkDelivery(BigDecimal amountKg, LocalDate date, SupplierNumber supplierNumber, TimeWindow timeWindow) {
        this.amountKg = amountKg;
        this.date = date;
        this.supplierNumber = supplierNumber;
        this.timeWindow = timeWindow;
    }

    public BigDecimal getAmountKg() {
        return amountKg;
    }

    public void setAmountKg(BigDecimal amountKg) {
        this.amountKg = amountKg;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public SupplierNumber getSupplierNumber() {
        return supplierNumber;
    }

    public void setSupplierNumber(SupplierNumber supplierNumber) {
        this.supplierNumber = supplierNumber;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }

    @Override
    public String toString() {
        return "MilkDelivery{" +
                "amountKg=" + amountKg +
                ", date=" + date +
                ", supplierNumber=" + supplierNumber.toString() +
                ", timeWindow=" + timeWindow +
                '}';
    }
}
