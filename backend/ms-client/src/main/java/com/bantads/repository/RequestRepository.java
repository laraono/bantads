package com.bantads.repository;

import com.bantads.entity.Client;
import com.bantads.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Request findByClient(Client client);

    Request findByClientCpf(String cpf);
}