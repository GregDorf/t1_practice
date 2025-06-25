package com.testprojgroup.t1_practice.repository;

import com.testprojgroup.t1_practice.model.Client;
import com.testprojgroup.t1_practice.model.ClientStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByClientId(UUID clientId);

    List<Client> findByStatus(ClientStatusEnum status, Pageable pageable);

    long countByStatus(ClientStatusEnum status);
}
