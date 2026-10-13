package com.bantads.repository;

import com.bantads.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    List<Manager> findAllByIsActiveOrderByNameAsc(boolean isActive);

    @Query("SELECT cpf FROM Manager WHERE isActive = true")
    List<String> findCPFs();

    Manager findByCpfAndIsActive(String cpf, boolean isActive);

    void deleteByCpf(String cpf);
}