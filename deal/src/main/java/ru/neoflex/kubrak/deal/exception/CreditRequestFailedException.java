package ru.neoflex.kubrak.deal.exception;

public class CreditRequestFailedException extends RuntimeException{

    public CreditRequestFailedException(String message){
        super(message);
    }
}
