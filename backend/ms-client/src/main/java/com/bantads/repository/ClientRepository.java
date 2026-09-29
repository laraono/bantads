package com.bantads.repository;

import com.bantads.entity.Client;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {
    
    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    Client findByCpf(String cpf);

    @Query("SELECT c FROM Client c WHERE " +
       "lower(c.name) LIKE lower(CONCAT('%', :busca, '%')) OR " +
       "c.cpf LIKE CONCAT('%', :busca, '%')")
    List<Client> search(@Param("busca") String busca);
}