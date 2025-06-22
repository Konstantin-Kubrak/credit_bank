package ru.neoflex.kubrak.deal.exception;

import java.util.UUID;

public class StatementNotFoundException extends Exception{

    public StatementNotFoundException(UUID uuid){
        super("Statement with ID %s not found".formatted(uuid));
    }
}
