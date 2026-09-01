package com.bantads.repository;

import com.bantads.entity.Auth;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends MongoRepository<Auth, UUID> 
{

    Optional<Auth> findByLogin(String login);

    boolean existsByLogin(String login);
}