package ru.neoflex.kubrak.gateway.model.entity;

import lombok.*;
import ru.neoflex.kubrak.gateway.model.enums.Gender;
import ru.neoflex.kubrak.gateway.model.enums.MaritalStatus;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class Client {

    private UUID clientId;
    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthDate;
    private String email;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private Integer dependentAmount;
    private Passport passport;
    private Employment employment;
    private String accountNumber;
}