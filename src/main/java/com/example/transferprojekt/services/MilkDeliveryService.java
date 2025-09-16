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

    public MilkDeliveryService(MilkDeliveryRepository milkDeliveryRepository) {
        this.milkDeliveryRepository = milkDeliveryRepository;
    }

    public MilkDeliveryEntity mapToEntity(MilkDelivery milkDelivery) {
        MilkDeliveryEntity entity = new MilkDeliveryEntity();
        entity.setAmountKg(milkDelivery.getAmountKg());
        entity.setDate(milkDelivery.getDate());
        entity.setTimeWindow(milkDelivery.getTimeWindow());
        //TODO hanlde supplierNr
        entity.setSupplierNr(new SupplierNrEntity());
        return entity;
    }

    public MilkDelivery mapToDataclass(MilkDeliveryEntity entity) {
        UUID deliveryId = entity.getDeliveryId();
        BigDecimal amountKg = entity.getAmountKg();
        LocalDate date = entity.getDate();
        TimeWindow timeWindow = entity.getTimeWindow();
        //TODO hanlde supplierNr
        //SupplierNumber supplierNumber = entity.getSupplierNr();
        return new MilkDelivery(deliveryId,amountKg,date,new SupplierNumber(99),timeWindow);
    }

    public List<MilkDelivery> getMilkDeliveries(){
        List<MilkDeliveryEntity> entities = milkDeliveryRepository.findAll();
        return entities.stream()
                .map(this::mapToDataclass)
                .toList();
    }

    public MilkDeliveryEntity getMilkDeliveryById(UUID deliveryId){
        return milkDeliveryRepository.findById(deliveryId).orElse(null);
    }

}