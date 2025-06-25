package com.testprojgroup.t1_practice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UnblockAccountResponse {
    private UUID accountId;
    private boolean allowUnblocking;
}