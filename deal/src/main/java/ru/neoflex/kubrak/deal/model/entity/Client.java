package ru.neoflex.kubrak.deal.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import ru.neoflex.kubrak.deal.model.enums.Gender;
import ru.neoflex.kubrak.deal.model.enums.MaritalStatus;
import ru.neoflex.kubrak.deal.model.jsonb.Employment;
import ru.neoflex.kubrak.deal.model.jsonb.Passport;
import ru.neoflex.kubrak.deal.model.jsonb.converter.EmploymentConverter;
import ru.neoflex.kubrak.deal.model.jsonb.converter.PassportConverter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "client")
public class Client {

    @Id
    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;

    @Column(name = "middle_name", length = 20)
    private String middleName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "dependent_amount")
    private Integer dependentAmount;

    @Convert(converter = PassportConverter.class)
    @Column(name = "passport", columnDefinition = "jsonb")
    private Passport passport;

    @Convert(converter = EmploymentConverter.class)
    @Column(name = "employment", columnDefinition = "jsonb")
    private Employment employment;

    @Column(name = "account_number", unique = true, length = 34)
    private String accountNumber;
}