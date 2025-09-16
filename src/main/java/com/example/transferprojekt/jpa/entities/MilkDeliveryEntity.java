package com.example.transferprojekt.jpa.entities;

import com.example.transferprojekt.enumerations.TimeWindow;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "milchlieferung")
public class MilkDeliveryEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "lieferung_id", updatable = false, nullable = false)
    private UUID deliveryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieferant_nr", nullable = false)
    private SupplierNrEntity supplierNr;

    @Column(name = "datum", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "zeitfenster", nullable = false)
    private TimeWindow timeWindow;

    @Column(name = "menge_kg")
    private BigDecimal amountKg;

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public SupplierNrEntity getSupplierNr() {
        return supplierNr;
    }

    public void setSupplierNr(SupplierNrEntity lieferantNr) {
        this.supplierNr = lieferantNr;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(TimeWindow zeitfenster) {
        this.timeWindow = zeitfenster;
    }

    public BigDecimal getAmountKg() {
        return amountKg;
    }

    public void setAmountKg(BigDecimal amountKg) {
        this.amountKg = amountKg;
    }
}
