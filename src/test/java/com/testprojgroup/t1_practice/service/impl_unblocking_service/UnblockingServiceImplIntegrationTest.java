package com.testprojgroup.t1_practice.service.impl_unblocking_service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.testprojgroup.t1_practice.jwt_auth.JwtUtil;
import com.testprojgroup.t1_practice.model.UnblockAccountResponse;
import com.testprojgroup.t1_practice.model.UnblockClientResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UnblockingServiceImplIntegrationTest {

    static WireMockServer wireMockServer;
    UnblockingServiceImpl unblockingService;

    @Mock
    JwtUtil mockJwtUtil;

    private final String MOCK_JWT_TOKEN = "integration-unblock-jwt-token";
    private String unblockClientUsernameJwt;

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        System.out.println("WireMock server for UnblockingService started on port: " + wireMockServer.port());
    }

    @AfterAll
    static void stopWireMockServer() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            System.out.println("WireMock server for UnblockingService stopped.");
        }
    }

    @BeforeEach
    void setUp() {
        WireMock.reset();
        unblockClientUsernameJwt = "t1_practice-unblock-task";
        Mockito.when(mockJwtUtil.generateToken(unblockClientUsernameJwt)).thenReturn(MOCK_JWT_TOKEN);

        unblockingService = new UnblockingServiceImpl(new RestTemplate(), mockJwtUtil);
        ReflectionTestUtils.setField(unblockingService, "bannedMonitorBaseUrl", "http://localhost:" + wireMockServer.port());
    }

    @Test
    @DisplayName("requestClientUnblock - success from WireMock")
    void requestClientUnblock_success() {
        UUID clientId = UUID.randomUUID();
        String expectedWireMockResponseJson = String.format(
                "{\"clientId\":\"%s\",\"allowUnblocking\":true}", clientId
        );

        stubFor(post(urlEqualTo("/api/unlock/clients/" + clientId.toString()))
                .withHeader("Authorization", equalTo("Bearer " + MOCK_JWT_TOKEN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedWireMockResponseJson)));

        UnblockClientResponse actualResponse = unblockingService.requestClientUnblock(clientId);

        assertNotNull(actualResponse);
        assertTrue(actualResponse.isAllowUnblocking());
        assertEquals(clientId, actualResponse.getClientId());

        verify(postRequestedFor(urlEqualTo("/api/unlock/clients/" + clientId.toString()))
                .withHeader("Authorization", equalTo("Bearer " + MOCK_JWT_TOKEN)));
    }

    @Test
    @DisplayName("requestClientUnblock - WireMock returns 403 Forbidden, service returns deny")
    void requestClientUnblock_wiremockReturns403() {
        UUID clientId = UUID.randomUUID();

        stubFor(post(urlEqualTo("/api/unlock/clients/" + clientId.toString()))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody("{\"error\":\"Forbidden by WireMock\"}")));
        UnblockClientResponse actualResponse = unblockingService.requestClientUnblock(clientId);

        assertNotNull(actualResponse);
        assertFalse(actualResponse.isAllowUnblocking());
        assertEquals(clientId, actualResponse.getClientId());

        verify(postRequestedFor(urlEqualTo("/api/unlock/clients/" + clientId.toString())));
    }

    @Test
    @DisplayName("requestAccountArrestRelease - success from WireMock")
    void requestAccountArrestRelease_success() {
        UUID accountId = UUID.randomUUID();
        String expectedWireMockResponseJson = String.format(
                "{\"accountId\":\"%s\",\"allowUnblocking\":true}", accountId
        );

        stubFor(post(urlEqualTo("/api/unlock/accounts/" + accountId.toString()))
                .withHeader("Authorization", equalTo("Bearer " + MOCK_JWT_TOKEN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedWireMockResponseJson)));
        UnblockAccountResponse actualResponse = unblockingService.requestAccountArrestRelease(accountId);

        assertNotNull(actualResponse);
        assertTrue(actualResponse.isAllowUnblocking());
        assertEquals(accountId, actualResponse.getAccountId());

        verify(postRequestedFor(urlEqualTo("/api/unlock/accounts/" + accountId.toString()))
                .withHeader("Authorization", equalTo("Bearer " + MOCK_JWT_TOKEN)));
    }

    @Test
    @DisplayName("requestAccountArrestRelease - WireMock returns 500, service returns deny")
    void requestAccountArrestRelease_wiremockReturns500() {
        UUID accountId = UUID.randomUUID();

        stubFor(post(urlEqualTo("/api/unlock/accounts/" + accountId.toString()))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"error\":\"Server error on WireMock\"}")));
        UnblockAccountResponse actualResponse = unblockingService.requestAccountArrestRelease(accountId);

        assertNotNull(actualResponse);
        assertFalse(actualResponse.isAllowUnblocking());
        assertEquals(accountId, actualResponse.getAccountId());

        verify(postRequestedFor(urlEqualTo("/api/unlock/accounts/" + accountId.toString())));
    }
}