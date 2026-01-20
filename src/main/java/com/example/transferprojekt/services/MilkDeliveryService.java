package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.*;
import com.example.transferprojekt.enumerations.TimeWindow;
import com.example.transferprojekt.jpa.entities.MilkDeliveryEntity;
import com.example.transferprojekt.jpa.entities.SupplierNrEntity;
import com.example.transferprojekt.jpa.repositories.MilkDeliveryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MilkDeliveryService {

    private final MilkDeliveryRepository milkDeliveryRepository;
    private final SupplierNrService supplierNrService;

    public MilkDeliveryService(MilkDeliveryRepository milkDeliveryRepository, SupplierNrService supplierNrService) {
        this.milkDeliveryRepository = milkDeliveryRepository;
        this.supplierNrService = supplierNrService;
    }

    /**
     * Saves a milk delivery (CREATE or UPDATE)
     * If delivery has an ID, it updates; otherwise creates new
     */
    public MilkDeliveryEntity save(MilkDelivery milkDelivery) {
        MilkDeliveryEntity entity;

        // Check if this is an update (has UUID) or create (no UUID)
        if (milkDelivery.getDeliveryId() != null) {
            // UPDATE: Load existing entity
            entity = milkDeliveryRepository.findById(milkDelivery.getDeliveryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "MilkDelivery not found for id: " + milkDelivery.getDeliveryId()));

            // Update fields
            entity.setAmountKg(milkDelivery.getAmountKg());
            entity.setDate(milkDelivery.getDate());
            entity.setTimeWindow(milkDelivery.getTimeWindow());

            // Update supplier number
            SupplierNumber supplierNumber = milkDelivery.getSupplierNumber();
            if (supplierNrService.exists(supplierNumber.getId())) {
                SupplierNrEntity supplierNrEntity = supplierNrService.getEntityById(supplierNumber.getId());
                entity.setSupplierNr(supplierNrEntity);
            } else {
                entity.setSupplierNr(supplierNrService.save(supplierNumber));
            }

        } else {
            // CREATE: New entity
            entity = mapToEntity(milkDelivery);
        }

        return milkDeliveryRepository.save(entity);
    }

    public MilkDeliveryEntity mapToEntity(MilkDelivery milkDelivery) {
        MilkDeliveryEntity entity = new MilkDeliveryEntity();
        entity.setAmountKg(milkDelivery.getAmountKg());
        entity.setDate(milkDelivery.getDate());
        entity.setTimeWindow(milkDelivery.getTimeWindow());

        SupplierNumber supplierNumber = milkDelivery.getSupplierNumber();
        if (supplierNrService.exists(supplierNumber.getId())) {
            /* fetch existing supplierNr from DB */
            SupplierNrEntity supplierNrEntity = supplierNrService.getEntityById(supplierNumber.getId());
            entity.setSupplierNr(supplierNrEntity);
        } else {
            /* create new supplierNr in DB */
            entity.setSupplierNr(supplierNrService.save(supplierNumber));
        }
        return entity;
    }

    public MilkDelivery mapToDataclass(MilkDeliveryEntity entity) {
        UUID deliveryId = entity.getDeliveryId();
        BigDecimal amountKg = entity.getAmountKg();
        LocalDate date = entity.getDate();
        TimeWindow timeWindow = entity.getTimeWindow();
        SupplierNumber supplierNumber = supplierNrService.mapToDataclass(entity.getSupplierNr());

        return new MilkDelivery(deliveryId, amountKg, date, supplierNumber, timeWindow);
    }

    public List<MilkDelivery> getDatabaseEntries(){
        List<MilkDeliveryEntity> entities = milkDeliveryRepository.findAll();
        return entities.stream()
                .map(this::mapToDataclass)
                .toList();
    }

    public MilkDeliveryEntity getById(UUID deliveryId){
        return milkDeliveryRepository.findById(deliveryId).orElse(null);
    }

    /**
     * Deletes a milk delivery by ID
     */
    public boolean deleteById(UUID deliveryId) {
        try {
            milkDeliveryRepository.deleteById(deliveryId);
            return true;
        } catch (Exception ex) {
            System.out.println("Exception while deleting milk delivery:");
            System.out.println(ex.getMessage());
            return false;
        }
    }
}