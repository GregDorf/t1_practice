package com.testprojgroup.transactionmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientStatusResponse {
    private String clientId;
    private String accountId;
    private String status;
}