package com.mdstore.connector.vietshare;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.mdstore.connector.SupplierException;
import com.mdstore.connector.SupplierProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test cho VietShareConnector.fetchCatalog().
 * Dùng WireMock để giả lập VietShare API — không cần kết nối internet thật.
 *
 * Xem docs/04-suppliers/vietshare.md — API 1 & Bảng Mã Lỗi
 */
class VietShareConnectorFetchCatalogTest {

    private WireMockServer wireMock;
    private VietShareConnector connector;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        VietShareProperties props = new VietShareProperties(
                "http://localhost:" + wireMock.port(),
                "test-api-key",
                "test-api-secret",
                5, 10
        );
        VietShareSigner signer = new VietShareSigner(props);
        ObjectMapper objectMapper = new ObjectMapper();
        connector = new VietShareConnector(props, signer, objectMapper);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    // ──────────── Happy path ────────────

    @Test
    void fetchCatalog_successfulResponse_returnsMappedProducts() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(okJson("""
                        {
                          "data": [
                            { "id": "VS_NORDVPN_1M", "name": "NordVPN 1 Tháng",
                              "price": 15000.00, "stock": 142, "is_active": true },
                            { "id": "VS_NETFLIX_1M", "name": "Netflix 1 Tháng",
                              "price": 45000.00, "stock": 0, "is_active": true }
                          ]
                        }
                        """)));

        List<SupplierProduct> products = connector.fetchCatalog();

        assertThat(products).hasSize(2);

        SupplierProduct nordvpn = products.get(0);
        assertThat(nordvpn.externalCode()).isEqualTo("VS_NORDVPN_1M");
        assertThat(nordvpn.supplyPrice()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(nordvpn.isActive()).isTrue();   // stock=142 > 0

        SupplierProduct netflix = products.get(1);
        assertThat(netflix.externalCode()).isEqualTo("VS_NETFLIX_1M");
        assertThat(netflix.isActive()).isFalse();  // stock=0 → isActive=false dù is_active=true
    }

    @Test
    void fetchCatalog_sendsCorrectAuthHeaders() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(okJson("{\"data\":[]}")));

        connector.fetchCatalog();

        // Xác nhận đúng 4 header xác thực (tên phải khớp docs/04-suppliers/vietshare.md)
        wireMock.verify(getRequestedFor(urlEqualTo("/v1/products"))
                .withHeader("X-Shop-API-ID", equalTo("test-api-key"))
                .withHeader("X-Timestamp", matching("\\d{13}"))   // Unix millis 13 digits
                .withHeader("X-Nonce", matching("[0-9a-f-]{36}")) // UUID
                .withHeader("X-Signature", matching("[0-9a-f]{64}")));
    }

    @Test
    void fetchCatalog_emptyDataArray_returnsEmptyList() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(okJson("{\"data\":[]}")));

        List<SupplierProduct> products = connector.fetchCatalog();

        assertThat(products).isEmpty();
    }

    @Test
    void fetchCatalog_nullDataField_returnsEmptyList() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(okJson("{\"data\":null}")));

        List<SupplierProduct> products = connector.fetchCatalog();

        assertThat(products).isEmpty();
    }

    // ──────────── is_active + stock logic ────────────

    @Test
    void fetchCatalog_stockZero_setsIsActiveFalse() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(okJson("""
                        {"data":[
                          {"id":"A","name":"A","price":1000,"stock":0,"is_active":true}
                        ]}
                        """)));

        List<SupplierProduct> products = connector.fetchCatalog();

        assertThat(products.get(0).isActive()).isFalse();
    }

    @Test
    void fetchCatalog_isActiveFalseWithStock_setsIsActiveFalse() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(okJson("""
                        {"data":[
                          {"id":"B","name":"B","price":1000,"stock":50,"is_active":false}
                        ]}
                        """)));

        List<SupplierProduct> products = connector.fetchCatalog();

        assertThat(products.get(0).isActive()).isFalse();
    }

    // ──────────── Error cases (ADR-006: retry rules) ────────────

    @Test
    void fetchCatalog_http401_throwsUnauthorized() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(unauthorized()));

        assertThatThrownBy(() -> connector.fetchCatalog())
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.UNAUTHORIZED);
                    assertThat(se.isRetryable()).isFalse();
                });
    }

    @Test
    void fetchCatalog_http429_throwsRateLimited_andIsRetryable() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(aResponse().withStatus(429).withBody("TOO_MANY_REQUESTS")));

        assertThatThrownBy(() -> connector.fetchCatalog())
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.RATE_LIMITED);
                    assertThat(se.isRetryable()).isTrue(); // ADR-006: 429 được retry
                });
    }

    @Test
    void fetchCatalog_http503_throwsServerError_andIsRetryable() {
        wireMock.stubFor(get(urlEqualTo("/v1/products"))
                .willReturn(aResponse().withStatus(503).withBody("SERVER_ERROR")));

        assertThatThrownBy(() -> connector.fetchCatalog())
                .isInstanceOf(SupplierException.class)
                .satisfies(e -> {
                    SupplierException se = (SupplierException) e;
                    assertThat(se.getErrorCode()).isEqualTo(SupplierException.ErrorCode.SERVER_ERROR);
                    assertThat(se.isRetryable()).isTrue(); // ADR-006: 5xx được retry
                });
    }
}
