package com.bantads.repository;

import com.bantads.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    List<Manager> findAllByIsActiveOrderByNameAsc(boolean isActive);

    Manager findByCpfAndIsActive(String cpf, boolean isActive);
}