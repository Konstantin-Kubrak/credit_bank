package ru.neoflex.kubrak.deal.exception;

import java.util.UUID;

public class StatementNotFoundException extends RuntimeException{

    public StatementNotFoundException(UUID uuid){
        super("Statement with ID %s not found".formatted(uuid));
    }
}
