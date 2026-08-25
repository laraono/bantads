package com.bantads.msauth.repository;
import java.util.List;
import java.util.Optional;

import com.bantads.msauth.entity.Auth;

public interface AuthRepository 
{
    Optional<Auth> findByLogin(String login);
    boolean existsByLogin(String login);
    Auth save(Auth auth);
    List<Auth> findAll();
}