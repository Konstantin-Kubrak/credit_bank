package ru.neoflex.kubrak.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.neoflex.kubrak.deal.model.entity.Client;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
