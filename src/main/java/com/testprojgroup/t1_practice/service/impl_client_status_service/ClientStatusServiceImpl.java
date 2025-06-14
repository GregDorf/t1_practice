package com.testprojgroup.t1_practice.service.impl_client_status_service;

import com.testprojgroup.t1_practice.jwt_auth.JwtUtil;
import com.testprojgroup.t1_practice.model.ClientStatusResponse;
import com.testprojgroup.t1_practice.service.ClientStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientStatusServiceImpl implements ClientStatusService {
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    public ClientStatusResponse fetchStatusFromService2(UUID clientId, UUID accountId) {
        String url = String.format("http://localhost:8081/api/client-status?clientId=%s&accountId=%s", clientId, accountId);

        try {
            String jwtToken = jwtUtil.generateToken("service1-client");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ClientStatusResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ClientStatusResponse.class
            );

            return response.getBody();
        } catch (Exception ex) {
            return null;
        }
    }
}
