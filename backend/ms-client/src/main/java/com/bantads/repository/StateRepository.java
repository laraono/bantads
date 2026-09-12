package com.bantads.repository;

import com.bantads.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
    State findByUf(String uf);
}