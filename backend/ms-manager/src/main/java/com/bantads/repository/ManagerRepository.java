package com.bantads.repository;

import com.bantads.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    List<Manager> findByManagerIdNotAndIsActive(Long id, boolean isActive);

    List<Manager> findAllByIsActive(boolean isActive);

    Manager findDistinctByManagerIdAndIsActive(Long id, boolean isActive);
}