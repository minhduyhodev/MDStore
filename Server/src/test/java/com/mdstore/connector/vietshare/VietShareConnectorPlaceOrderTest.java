package com.mdstore.connector.vietshare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mdstore.connector.OrderRequest;
import com.mdstore.connector.OrderResult;
import com.mdstore.connector.SupplierException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test cho VietShareConnector.placeOrder().
 * Dùng WireMock để giả lập VietShare API.
 */
class VietShareConnectorPlaceOrderTest {

    private WireMockServer wireMock;
    private VietShareConnector connector;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        
        objectMapper = new ObjectMapper();

        VietShareProperties props = new VietShareProperties(
                "http://localhost:" + wireMock.port() + "/v1",
                "test-api-key",
                "test-api-secret",
                5, 10
        );
        VietShareSigner signer = new VietShareSigner(props);
        connector = new VietShareConnector(props, signer, objectMapper);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    // ──────────── Happy path ────────────

    @Test
    void placeOrder_successfulResponse_returnsMappedResult() {
        String idempotencyKey = UUID.randomUUID().toString();
        
        wireMock.stubFor(post(urlEqualTo("/v1/orders"))
                .withHeader("Idempotency-Key", equalTo(idempotencyKey))
                .willReturn(okJson("""
                        {
                          "status": "success",
                          "data": {
                            "order_id": "VS-987654321",
                            "product_code": "VS_NORDVPN_1M",
                            "unit_price": 15000.00,
                            "total_price": 15000.00,
                            "delivered_accounts": [
                              {
                                "type": "EMAIL_PASS",
                                "data": "user@example.com|pass123"
                              }
                            ]
                          }
                        }
                        """)));

        OrderRequest request = new OrderRequest("VS_NORDVPN_1M", 1, new BigDecimal("15000.00"), idempotencyKey, null, null);
        
        OrderResult result = connector.placeOrder(request);

        assertThat(result.supplierOrderId()).isEqualTo("VS-987654321");
        assertThat(result.unitPrice()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(result.accountData()).hasSize(1);
        assertThat(result.accountData().get(0)).isEqualTo("user@example.com|pass123");
        
        wireMock.verify(postRequestedFor(urlEqualTo("/v1/orders"))
                .withHeader("X-Shop-API-ID", equalTo("test-api-key"))
                .withHeader("X-Timestamp", matching("\\d{10}"))
                .withHeader("X-Nonce", matching("[0-9a-f-]{36}"))
                .withHeader("X-Signature", matching("[0-9a-f]{64}")));
    }

    // ──────────── Error cases ────────────

    @Test
    void placeOrder_repeatedCalls_preserveIdempotencyKey() {
        String idempotencyKey = "stable-idempotency-key";
        wireMock.stubFor(post(urlEqualTo("/v1/orders"))
                .withHeader("Idempotency-Key", equalTo(idempotencyKey))
                .willReturn(okJson("""
                        {
                          "status": "success",
                          "data": {
                            "order_id": "VS-1",
                            "product_code": "VS_NORDVPN_1M",
                            "unit_price": 15000.00,
                            "total_price": 15000.00,
                            "delivered_accounts": []
                          }
                        }
                        """)));
        OrderRequest request = new OrderRequest("VS_NORDVPN_1M", 1,
                new BigDecimal("15000.00"), idempotencyKey, null, null);

        connector.placeOrder(request);
        connector.placeOrder(request);

        wireMock.verify(2, postRequestedFor(urlEqualTo("/v1/orders"))
                .withHeader("Idempotency-Key", equalTo(idempotencyKey)));
    }

    @Test
    void placeOrder_http409_priceChanged_throwsPriceChanged() {
        wireMock.stubFor(post(urlEqualTo("/v1/orders"))
                .willReturn(aResponse().withStatus(409).withBody("PRICE_CHANGED")));

        OrderRequest request = new OrderRequest("VS_NORDVPN_1M", 1, new BigDecimal("10000.00"), UUID.randomUUID().toString(), null, null);

        assertThatThrownBy(() -> connector.placeOrder(request))
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.PRICE_CHANGED);
                    assertThat(se.isRetryable()).isFalse();
                });
    }

    @Test
    void placeOrder_http409_outOfStock_throwsOutOfStock() {
        wireMock.stubFor(post(urlEqualTo("/v1/orders"))
                .willReturn(aResponse().withStatus(409).withBody("OUT_OF_STOCK")));

        OrderRequest request = new OrderRequest("VS_NORDVPN_1M", 1, new BigDecimal("15000.00"), UUID.randomUUID().toString(), null, null);

        assertThatThrownBy(() -> connector.placeOrder(request))
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.OUT_OF_STOCK);
                    assertThat(se.isRetryable()).isFalse();
                });
    }

    @Test
    void placeOrder_http429_throwsRateLimitedWithRetryAfter() {
        wireMock.stubFor(post(urlEqualTo("/v1/orders"))
                .willReturn(aResponse().withStatus(429)
                        .withHeader("Retry-After", "20")
                        .withBody("TOO_MANY_REQUESTS")));

        OrderRequest request = new OrderRequest("VS_NORDVPN_1M", 1, new BigDecimal("15000.00"), UUID.randomUUID().toString(), null, null);

        assertThatThrownBy(() -> connector.placeOrder(request))
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.RATE_LIMITED);
                    assertThat(se.isRetryable()).isTrue();
                    assertThat(se.getRetryAfter()).contains(java.time.Duration.ofSeconds(20));
                });
    }

    @Test
    void placeOrder_requestInProgress_isRetryableWithRetryAfter() {
        wireMock.stubFor(post(urlEqualTo("/v1/orders"))
                .willReturn(aResponse().withStatus(409)
                        .withHeader("Retry-After", "12")
                        .withBody("REQUEST_IN_PROGRESS")));

        OrderRequest request = new OrderRequest("VS_NORDVPN_1M", 1, new BigDecimal("15000.00"), UUID.randomUUID().toString(), null, null);

        assertThatThrownBy(() -> connector.placeOrder(request))
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.REQUEST_IN_PROGRESS);
                    assertThat(se.isRetryable()).isTrue();
                    assertThat(se.getRetryAfter()).contains(java.time.Duration.ofSeconds(12));
                });
    }

    @Test
    void placeOrder_http402_throwsInvalidRequest_notRetryable() {
        wireMock.stubFor(post(urlEqualTo("/v1/orders"))
                .willReturn(aResponse().withStatus(402).withBody("PAYMENT_REQUIRED")));

        OrderRequest request = new OrderRequest("VS_NORDVPN_1M", 1, new BigDecimal("15000.00"), UUID.randomUUID().toString(), null, null);

        assertThatThrownBy(() -> connector.placeOrder(request))
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.INVALID_REQUEST);
                    assertThat(se.isRetryable()).isFalse();
                });
    }

    @Test
    void placeOrder_http503_throwsServerError_isRetryable() {
        wireMock.stubFor(post(urlEqualTo("/v1/orders"))
                .willReturn(aResponse().withStatus(503).withBody("SERVER_ERROR")));

        OrderRequest request = new OrderRequest("VS_NORDVPN_1M", 1, new BigDecimal("15000.00"), UUID.randomUUID().toString(), null, null);

        assertThatThrownBy(() -> connector.placeOrder(request))
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.SERVER_ERROR);
                    assertThat(se.isRetryable()).isTrue();
                });
    }
}
