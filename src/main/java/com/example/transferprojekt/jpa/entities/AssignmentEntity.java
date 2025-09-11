package com.example.transferprojekt.jpa.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "zuweisung")
public class AssignmentEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "zuweisung_id", updatable = false, nullable = false)
    private UUID assignmentId;

    @ManyToOne
    @JoinColumn(name = "lieferant_nr", nullable = false)
    private SupplierNrEntity supplierNr;

    @ManyToOne
    @JoinColumn(name = "lieferant_id", nullable = false)
    private SupplierEntity supplierId;

    @Column(name = "zugewiesen_ab", nullable = false)
    private LocalDate assignmentStartDate;

    @Column(name = "zugewiesen_bis")
    private LocalDate assignmentEndDate;
}
