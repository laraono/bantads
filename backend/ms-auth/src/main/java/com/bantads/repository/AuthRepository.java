package com.bantads.repository;

import com.bantads.entity.Auth;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends MongoRepository<Auth, String> 
{

    Optional<Auth> findByEmail(String email);

    boolean existsByEmail(String email);

    Auth findByCpf(String cpf);
    
    void deleteByEmail(String email);

    void deleteByCpf(String cpf);
}