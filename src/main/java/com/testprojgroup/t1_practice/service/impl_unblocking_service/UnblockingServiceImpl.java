package com.testprojgroup.t1_practice.service.impl_unblocking_service;

import com.testprojgroup.t1_practice.jwt_auth.JwtUtil;
import com.testprojgroup.t1_practice.model.UnblockAccountResponse;
import com.testprojgroup.t1_practice.model.UnblockClientResponse;
import com.testprojgroup.t1_practice.service.UnblockingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class UnblockingServiceImpl implements UnblockingService {
    private static final Logger log = LoggerFactory.getLogger(UnblockingServiceImpl.class);
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    @Value("${bannedmonitor.url}")
    private String bannedMonitorBaseUrl;

    private static final String UNBLOCK_CLIENT_USERNAME_JWT = "t1_practice-unblock-task";

    @Autowired
    public UnblockingServiceImpl(RestTemplate restTemplate, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.jwtUtil = jwtUtil;
    }

    public UnblockClientResponse requestClientUnblock(UUID clientId) {
        String url = bannedMonitorBaseUrl + "/api/unlock/clients/" + clientId.toString();

        try {
            String jwtToken = jwtUtil.generateToken(UNBLOCK_CLIENT_USERNAME_JWT);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<UnblockClientResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    UnblockClientResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("HTTP error during client unblock request for {}: {} - {}", clientId, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            log.error("Error during client unblock request for {}: {}", clientId, e.getMessage(), e);
        }
        UnblockClientResponse errorResponse = new UnblockClientResponse();
        errorResponse.setClientId(clientId);
        errorResponse.setAllowUnblocking(false);
        return errorResponse;
    }

    public UnblockAccountResponse requestAccountArrestRelease(UUID accountId) {
        String url = bannedMonitorBaseUrl + "/api/unlock/accounts/" + accountId.toString();

        try {
            String jwtToken = jwtUtil.generateToken(UNBLOCK_CLIENT_USERNAME_JWT);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<UnblockAccountResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    UnblockAccountResponse.class
            );
            log.info("Received arrest release decision for account {}: {}", accountId, response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("HTTP error during account arrest release request for {}: {} - {}", accountId, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            log.error("Error during account arrest release request for {}: {}", accountId, e.getMessage(), e);
        }
        UnblockAccountResponse errorResponse = new UnblockAccountResponse();
        errorResponse.setAccountId(accountId);
        errorResponse.setAllowUnblocking(false);
        return errorResponse;
    }
}
