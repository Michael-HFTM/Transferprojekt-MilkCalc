package com.example.transferprojekt.jpa.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "Zuweisung")
public class AssignmentEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "ZuweisungID", updatable = false, nullable = false)
    private UUID assignmentId;

    @ManyToOne
    @JoinColumn(name = "LieferantNr", nullable = false)
    private SupplierNrEntity supplierNr;

    @ManyToOne
    @JoinColumn(name = "LieferantID", nullable = false)
    private SupplierEntity supplierId;

    @Column(name = "ZugewiesenAb", nullable = false)
    private LocalDate assignmentStartDate;

    @Column(name = "ZugewiesenBis")
    private LocalDate assignmentEndDate;
}
