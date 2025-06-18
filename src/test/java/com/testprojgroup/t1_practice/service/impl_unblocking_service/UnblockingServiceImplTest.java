package com.testprojgroup.t1_practice.service.impl_unblocking_service;

import com.testprojgroup.t1_practice.jwt_auth.JwtUtil;
import com.testprojgroup.t1_practice.model.UnblockAccountResponse;
import com.testprojgroup.t1_practice.model.UnblockClientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnblockingServiceImplTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private UnblockingServiceImpl unblockingService;

    private final String BASE_URL = "http://fake-banned-monitor.com";
    private final String MOCK_JWT_TOKEN = "mock.unblock.jwt.token";
    private String unblockClientUsernameJwt;


    @BeforeEach
    void setUp() {
        unblockClientUsernameJwt = "t1_practice-unblock-task";

        ReflectionTestUtils.setField(unblockingService, "bannedMonitorBaseUrl", BASE_URL);
        when(jwtUtil.generateToken(unblockClientUsernameJwt)).thenReturn(MOCK_JWT_TOKEN);
    }

    @Test
    @DisplayName("requestClientUnblock - success")
    void requestClientUnblock_success() {
        UUID clientId = UUID.randomUUID();
        String url = BASE_URL + "/api/unlock/clients/" + clientId.toString();

        UnblockClientResponse expectedApiResp = new UnblockClientResponse();
        expectedApiResp.setClientId(clientId);
        expectedApiResp.setAllowUnblocking(true);
        ResponseEntity<UnblockClientResponse> mockResponseEntity = new ResponseEntity<>(expectedApiResp, HttpStatus.OK);

        ArgumentCaptor<HttpEntity<Void>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(UnblockClientResponse.class)))
                .thenReturn(mockResponseEntity);

        UnblockClientResponse actualResponse = unblockingService.requestClientUnblock(clientId);

        assertNotNull(actualResponse);
        assertTrue(actualResponse.isAllowUnblocking());
        assertEquals(clientId, actualResponse.getClientId());
        assertEquals("Bearer " + MOCK_JWT_TOKEN, httpEntityCaptor.getValue().getHeaders().getFirst("Authorization"));
        verify(jwtUtil).generateToken(unblockClientUsernameJwt);
    }

    @Test
    @DisplayName("requestClientUnblock - HttpClientError returns deny response")
    void requestClientUnblock_httpClientError_returnsDeny() {
        UUID clientId = UUID.randomUUID();
        String url = BASE_URL + "/api/unlock/clients/" + clientId.toString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(UnblockClientResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Client error"));

        UnblockClientResponse actualResponse = unblockingService.requestClientUnblock(clientId);

        assertNotNull(actualResponse);
        assertFalse(actualResponse.isAllowUnblocking());
        assertEquals(clientId, actualResponse.getClientId());
        verify(jwtUtil).generateToken(unblockClientUsernameJwt);
    }

    @Test
    @DisplayName("requestClientUnblock - RestClientError (e.g. network) returns deny response")
    void requestClientUnblock_restClientError_returnsDeny() {
        UUID clientId = UUID.randomUUID();
        String url = BASE_URL + "/api/unlock/clients/" + clientId.toString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(UnblockClientResponse.class)))
                .thenThrow(new RestClientException("Network connection failed"));

        UnblockClientResponse actualResponse = unblockingService.requestClientUnblock(clientId);

        assertNotNull(actualResponse);
        assertFalse(actualResponse.isAllowUnblocking());
        assertEquals(clientId, actualResponse.getClientId());
        verify(jwtUtil).generateToken(unblockClientUsernameJwt);
    }


    @Test
    @DisplayName("requestAccountArrestRelease - success")
    void requestAccountArrestRelease_success() {
        UUID accountId = UUID.randomUUID();
        String url = BASE_URL + "/api/unlock/accounts/" + accountId.toString();

        UnblockAccountResponse expectedApiResp = new UnblockAccountResponse();
        expectedApiResp.setAccountId(accountId);
        expectedApiResp.setAllowUnblocking(true);
        ResponseEntity<UnblockAccountResponse> mockResponseEntity = new ResponseEntity<>(expectedApiResp, HttpStatus.OK);

        ArgumentCaptor<HttpEntity<Void>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(UnblockAccountResponse.class)))
                .thenReturn(mockResponseEntity);

        UnblockAccountResponse actualResponse = unblockingService.requestAccountArrestRelease(accountId);

        assertNotNull(actualResponse);
        assertTrue(actualResponse.isAllowUnblocking());
        assertEquals(accountId, actualResponse.getAccountId());
        assertEquals("Bearer " + MOCK_JWT_TOKEN, httpEntityCaptor.getValue().getHeaders().getFirst("Authorization"));
        verify(jwtUtil).generateToken(unblockClientUsernameJwt);
    }

    @Test
    @DisplayName("requestAccountArrestRelease - HttpClientError returns deny response")
    void requestAccountArrestRelease_httpClientError_returnsDeny() {
        UUID accountId = UUID.randomUUID();
        String url = BASE_URL + "/api/unlock/accounts/" + accountId.toString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(UnblockAccountResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access denied"));

        UnblockAccountResponse actualResponse = unblockingService.requestAccountArrestRelease(accountId);

        assertNotNull(actualResponse);
        assertFalse(actualResponse.isAllowUnblocking());
        assertEquals(accountId, actualResponse.getAccountId());
        verify(jwtUtil).generateToken(unblockClientUsernameJwt);
    }
}