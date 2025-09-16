package com.example.transferprojekt.jpa.repositories;

import com.example.transferprojekt.jpa.entities.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<SupplierEntity, UUID> {

}