package com.testprojgroup.t1_practice.service.impl_client_status_service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.testprojgroup.t1_practice.jwt_auth.JwtUtil;
import com.testprojgroup.t1_practice.model.ClientStatusResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class ClientStatusServiceImplIntegrationTest {

    static WireMockServer wireMockServer;
    ClientStatusServiceImpl clientStatusService;

    @Mock
    JwtUtil mockJwtUtil;

    private final String MOCK_JWT_TOKEN = "integration-test-jwt-token";
    private final String SERVICE_USER = "service1-client";

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
        System.out.println("WireMock server for ClientStatusService started on port 8081");
    }

    @AfterAll
    static void stopWireMockServer() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            System.out.println("WireMock server for ClientStatusService stopped.");
        }
    }

    @BeforeEach
    void setUp() {
        WireMock.reset();
        Mockito.when(mockJwtUtil.generateToken(SERVICE_USER)).thenReturn(MOCK_JWT_TOKEN);

        clientStatusService = new ClientStatusServiceImpl(new RestTemplate(), mockJwtUtil);
    }

    @Test
    @DisplayName("fetchStatusFromService2 - success - should get status from WireMock")
    void fetchStatusFromService2_success() {
        UUID clientId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        String expectedStatus = "ACTIVE_FROM_WIREMOCK";

        String mockJsonResponse = String.format(
                "{\"clientId\":\"%s\",\"accountId\":\"%s\",\"status\":\"%s\"}",
                clientId.toString(), accountId.toString(), expectedStatus
        );

        stubFor(get(urlPathEqualTo("/api/client-status"))
                .withQueryParam("clientId", equalTo(clientId.toString()))
                .withQueryParam("accountId", equalTo(accountId.toString()))
                .withHeader("Authorization", equalTo("Bearer " + MOCK_JWT_TOKEN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));

        ClientStatusResponse actualResponse = clientStatusService.fetchStatusFromService2(clientId, accountId);

        assertNotNull(actualResponse);
        assertEquals(clientId.toString(), actualResponse.getClientId());
        assertEquals(accountId.toString(), actualResponse.getAccountId());
        assertEquals(expectedStatus, actualResponse.getStatus());

        verify(getRequestedFor(urlPathEqualTo("/api/client-status"))
                .withQueryParam("clientId", equalTo(clientId.toString()))
                .withQueryParam("accountId", equalTo(accountId.toString()))
                .withHeader("Authorization", equalTo("Bearer " + MOCK_JWT_TOKEN)));
    }

    @Test
    @DisplayName("fetchStatusFromService2 - 404 Not Found from WireMock should return null")
    void fetchStatusFromService2_wiremockReturns404() {
        UUID clientId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        stubFor(get(urlPathEqualTo("/api/client-status"))
                .withQueryParam("clientId", equalTo(clientId.toString()))
                .withQueryParam("accountId", equalTo(accountId.toString()))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"error\":\"Client or account not found\"}")));

        ClientStatusResponse actualResponse = clientStatusService.fetchStatusFromService2(clientId, accountId);

        assertNull(actualResponse);

        verify(getRequestedFor(urlPathEqualTo("/api/client-status"))
                .withQueryParam("clientId", equalTo(clientId.toString()))
                .withQueryParam("accountId", equalTo(accountId.toString())));
    }

    @Test
    @DisplayName("fetchStatusFromService2 - 500 Server Error from WireMock should return null")
    void fetchStatusFromService2_wiremockReturns500() {
        UUID clientId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        stubFor(get(urlPathEqualTo("/api/client-status"))
                .withQueryParam("clientId", equalTo(clientId.toString()))
                .withQueryParam("accountId", equalTo(accountId.toString()))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error from WireMock")));

        ClientStatusResponse actualResponse = clientStatusService.fetchStatusFromService2(clientId, accountId);
        assertNull(actualResponse);

        verify(getRequestedFor(urlPathEqualTo("/api/client-status"))
                .withQueryParam("clientId", equalTo(clientId.toString()))
                .withQueryParam("accountId", equalTo(accountId.toString())));
    }
}