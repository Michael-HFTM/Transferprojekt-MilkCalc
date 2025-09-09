package com.example.transferprojekt.jpa.entities;

import com.example.transferprojekt.enumerations.TimeWindow;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "Milchlieferung")
public class MilkDelivery {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "LieferungID", updatable = false, nullable = false)
    private UUID deliveryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LieferantNr", nullable = false)
    private SupplierNrEntity lieferantNr;

    @Column(name = "Datum", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "Zeitfenster", nullable = false)
    private TimeWindow zeitfenster;

    @Column(name = "MengeKg")
    private BigDecimal amountKg;

}
