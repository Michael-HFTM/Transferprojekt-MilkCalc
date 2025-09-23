package com.example.transferprojekt.services;

import com.example.transferprojekt.dataclasses.*;
import com.example.transferprojekt.enumerations.TimeWindow;
import com.example.transferprojekt.jpa.entities.MilkDeliveryEntity;
import com.example.transferprojekt.jpa.entities.SupplierNrEntity;
import com.example.transferprojekt.jpa.repositories.MilkDeliveryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MilkDeliveryService {

    private final MilkDeliveryRepository milkDeliveryRepository;
    private final SupplierNrService supplierNrService;

    public MilkDeliveryService(MilkDeliveryRepository milkDeliveryRepository, SupplierNrService supplierNrService) {
        this.milkDeliveryRepository = milkDeliveryRepository;
        this.supplierNrService = supplierNrService;

    }

    public MilkDeliveryEntity save(MilkDelivery milkDelivery) {
        MilkDeliveryEntity entity = mapToEntity(milkDelivery);
        return milkDeliveryRepository.save(entity);
    }

    public MilkDeliveryEntity mapToEntity(MilkDelivery milkDelivery) {
        MilkDeliveryEntity entity = new MilkDeliveryEntity();
        entity.setAmountKg(milkDelivery.getAmountKg());
        entity.setDate(milkDelivery.getDate());
        entity.setTimeWindow(milkDelivery.getTimeWindow());

        SupplierNumber supplierNumber = milkDelivery.getSupplierNumber();
        if (supplierNrService.exists(supplierNumber.getId())) {
            /* fetch exisiting supplierNr from DB */
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

        return new MilkDelivery(deliveryId,amountKg,date,supplierNumber,timeWindow);
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

}