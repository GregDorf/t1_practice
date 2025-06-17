package com.testprojgroup.bannedmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnblockClientResponse {
    private UUID clientId;
    private boolean allowUnblocking;
}