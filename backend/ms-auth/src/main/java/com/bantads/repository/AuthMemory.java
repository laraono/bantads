package com.bantads.repository;

import org.springframework.stereotype.Repository;

import com.bantads.entity.Auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class AuthMemory implements AuthRepository 
{
    private final ConcurrentMap<String, Auth> users = new ConcurrentHashMap<>();

    @Override
    public Optional<Auth> findByLogin(String login) 
    {
        return Optional.ofNullable(users.get(login));
    }
    @Override
    public boolean existsByLogin(String login) 
    {
        return users.containsKey(login);
    }
    @Override
    public Auth save(Auth auth) 
    {
        if (auth.getUserId() == null) 
        {
            auth.setUserId(UUID.randomUUID());
        }

        users.put(auth.getLogin(), auth);
        return auth;
    }
    @Override
    public List<Auth> findAll() 
    {
        return new ArrayList<>(users.values());
    }
}