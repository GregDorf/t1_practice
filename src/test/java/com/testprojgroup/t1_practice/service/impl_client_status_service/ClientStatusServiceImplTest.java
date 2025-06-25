package com.testprojgroup.t1_practice.service.impl_client_status_service;

import com.testprojgroup.t1_practice.jwt_auth.JwtUtil;
import com.testprojgroup.t1_practice.model.ClientStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientStatusServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ClientStatusServiceImpl clientStatusService;

    private UUID clientUuid;
    private UUID accountUuid;
    private final String MOCK_JWT_TOKEN = "mock.jwt.token";
    private final String SERVICE_USER = "service1-client";

    @BeforeEach
    void setUp() {
        clientUuid = UUID.randomUUID();
        accountUuid = UUID.randomUUID();
        when(jwtUtil.generateToken(SERVICE_USER)).thenReturn(MOCK_JWT_TOKEN);
    }

    @Test
    @DisplayName("fetchStatusFromService2 - success - should return status and verify interaction")
    void fetchStatusFromService2_success() {
        String expectedUrl = String.format("http:localhost:8081apiclient-status?clientId=%s&accountId=%s", clientUuid, accountUuid);
        ClientStatusResponse expectedApiResponse = new ClientStatusResponse(
                clientUuid.toString(),
                accountUuid.toString(),
                "ACTIVE"
        );
        ResponseEntity<ClientStatusResponse> mockResponseEntity = new ResponseEntity<>(expectedApiResponse, HttpStatus.OK);

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("Authorization", "Bearer " + MOCK_JWT_TOKEN);
        HttpEntity<Void> expectedHttpEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedHttpEntity),
                eq(ClientStatusResponse.class)
        )).thenReturn(mockResponseEntity);

        ClientStatusResponse actualResponse = clientStatusService.fetchStatusFromService2(clientUuid, accountUuid);

        assertNotNull(actualResponse, "Response should not be null on success");
        assertEquals(clientUuid.toString(), actualResponse.getClientId());
        assertEquals(accountUuid.toString(), actualResponse.getAccountId());
        assertEquals("ACTIVE", actualResponse.getStatus());

        verify(jwtUtil).generateToken(SERVICE_USER);
        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedHttpEntity),
                eq(ClientStatusResponse.class)
        );
    }

    @Test
    @DisplayName("fetchStatusFromService2 - RestClientException from RestTemplate should return null")
    void fetchStatusFromService2_restClientException_returnsNull() {
        String expectedUrl = String.format("http:localhost:8081apiclient-status?clientId=%s&accountId=%s", clientUuid, accountUuid);

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.set("Authorization", "Bearer " + MOCK_JWT_TOKEN);
        HttpEntity<Void> expectedHttpEntity = new HttpEntity<>(expectedHeaders);

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedHttpEntity),
                eq(ClientStatusResponse.class)
        )).thenThrow(new RestClientException("Simulated API communication error"));

        ClientStatusResponse actualResponse = clientStatusService.fetchStatusFromService2(clientUuid, accountUuid);

        assertNull(actualResponse, "Response should be null when RestClientException occurs");

        verify(jwtUtil).generateToken(SERVICE_USER);
        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(expectedHttpEntity),
                eq(ClientStatusResponse.class)
        );
    }

    @Test
    @DisplayName("fetchStatusFromService2 - JWT generation fails should return null and not call RestTemplate")
    void fetchStatusFromService2_jwtFails_returnsNull() {
        when(jwtUtil.generateToken(SERVICE_USER)).thenThrow(new RuntimeException("Simulated JWT generation failed"));

        ClientStatusResponse actualResponse = clientStatusService.fetchStatusFromService2(clientUuid, accountUuid);

        assertNull(actualResponse, "Response should be null when JWT generation fails");

        verify(jwtUtil).generateToken(SERVICE_USER);

        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        );
    }
}