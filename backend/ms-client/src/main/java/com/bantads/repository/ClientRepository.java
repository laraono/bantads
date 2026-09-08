package com.bantads.repository;

import com.bantads.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    
    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    Client findByCpf(String cpf);
}