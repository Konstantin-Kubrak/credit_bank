package ru.neoflex.kubrak.gateway.model.entity;


import lombok.Builder;
import lombok.Data;
import ru.neoflex.kubrak.gateway.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.gateway.model.enums.ChangeType;

import java.sql.Timestamp;

@Data
@Builder
public class StatusHistory {

    private ApplicationStatus status;
    private Timestamp time;
    private ChangeType changeType;
}
