package com.dws.challenge.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String accountFromId;
    private String accountToId;
    private BigDecimal amount;
}