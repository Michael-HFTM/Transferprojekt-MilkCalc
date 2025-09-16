package com.example.transferprojekt.jpa.repositories;

import com.example.transferprojekt.jpa.entities.MilkDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MilkDeliveryRepository extends JpaRepository<MilkDeliveryEntity, UUID> {

}