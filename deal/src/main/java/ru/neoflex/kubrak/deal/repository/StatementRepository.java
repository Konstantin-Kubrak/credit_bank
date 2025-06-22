package ru.neoflex.kubrak.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.neoflex.kubrak.deal.model.entity.Statement;

import java.util.UUID;

@Repository
public interface StatementRepository extends JpaRepository<Statement, UUID> {


}
