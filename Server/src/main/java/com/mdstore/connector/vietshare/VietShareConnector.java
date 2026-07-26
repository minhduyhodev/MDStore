package com.mdstore.connector.vietshare;

import com.mdstore.connector.OrderRequest;
import com.mdstore.connector.OrderResult;
import com.mdstore.connector.SupplierConnector;
import com.mdstore.connector.SupplierException;
import com.mdstore.connector.SupplierException.ErrorCode;
import com.mdstore.connector.SupplierProduct;
import com.mdstore.connector.vietshare.VietShareSigner.SignedHeaders;
import com.mdstore.connector.vietshare.dto.VsOrderRequest;
import com.mdstore.connector.vietshare.dto.VsOrderResponse;
import com.mdstore.connector.vietshare.dto.VsOrderResponse.VsDeliveredAccount;
import com.mdstore.connector.vietshare.dto.VsProductListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Connector cho VietShare — implement SupplierConnector (ADR-004).
 * Xử lý ký số HMAC-SHA256 (ADR-003), retry nằm ở tầng service gọi vào đây (ADR-006).
 *
 * API docs: docs/04-suppliers/vietshare.md
 */
@Component
public class VietShareConnector implements SupplierConnector {

    private static final Logger log = LoggerFactory.getLogger(VietShareConnector.class);
    static final String SUPPLIER_CODE = "VIETSHARE";

    private final RestClient restClient;
    private final VietShareSigner signer;
    private final ObjectMapper objectMapper;
    private final URI apiBaseUri;

    public VietShareConnector(VietShareProperties props, VietShareSigner signer, ObjectMapper objectMapper) {
        this.signer = signer;
        this.objectMapper = objectMapper;
        this.apiBaseUri = normalizeApiBaseUri(props.apiUrl());

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(props.connectTimeoutSeconds()).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(props.readTimeoutSeconds()).toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    private URI normalizeApiBaseUri(String configuredUrl) {
        String normalized = configuredUrl.replaceAll("/+$", "");
        if (normalized.endsWith("/v1")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        return URI.create(normalized);
    }

    private URI endpoint(String path) {
        return apiBaseUri.resolve(path);
    }

    @Override
    public String getSupplierCode() {
        return SUPPLIER_CODE;
    }

    // ──────────────────────────────────────────────────────
    // fetchCatalog — GET /v1/products
    // ──────────────────────────────────────────────────────

    /**
     * Lấy toàn bộ catalog sản phẩm từ VietShare.
     * Được gọi bởi CatalogSyncService mỗi 10-30 giây (Flow 3).
     *
     * - field price = giá nhập MDStore (giá VietShare đã gồm markup của họ)
     * - isActive = item.isActive && item.stock > 0
     *
     * @throws SupplierException(UNAUTHORIZED) nếu API key/signature sai
     * @throws SupplierException(RATE_LIMITED) nếu vượt 60 req/min → CatalogSyncService giữ cache cũ
     * @throws SupplierException(SERVER_ERROR) nếu VietShare 5xx
     * @throws SupplierException(NETWORK_TIMEOUT) nếu timeout
     */
    @Override
    public List<SupplierProduct> fetchCatalog() {
        log.debug("Fetching catalog from VietShare: GET /v1/products");

        SignedHeaders headers = signer.sign("GET", "/v1/products", null);

        try {
            VsProductListResponse response = restClient.get()
                    .uri(endpoint("/v1/products"))
                    .header("X-Shop-API-ID", headers.xShopApiId())
                    .header("X-Timestamp",   headers.xTimestamp())
                    .header("X-Nonce",       headers.xNonce())
                    .header("X-Signature",   headers.xSignature())
                    .retrieve()
                    .body(VsProductListResponse.class);

            if (response == null || response.data() == null) {
                log.warn("VietShare returned empty catalog response");
                return List.of();
            }

            List<SupplierProduct> products = response.data().stream()
                    .map(item -> new SupplierProduct(
                            item.id(),
                            item.name(),
                            item.price(),
                            item.isActive() && item.stock() > 0,
                            item.flashSaleId()
                    ))
                    .toList();

            log.debug("Fetched {} products from VietShare ({} active)",
                    products.size(),
                    products.stream().filter(SupplierProduct::isActive).count());

            return products;

        } catch (HttpClientErrorException e) {
            return handleClientError(e, "fetchCatalog");
        } catch (HttpServerErrorException e) {
            log.warn("VietShare server error during fetchCatalog: {}", e.getStatusCode());
            throw new SupplierException(ErrorCode.SERVER_ERROR,
                    "VietShare server error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.warn("VietShare timeout during fetchCatalog", e);
            throw new SupplierException(ErrorCode.NETWORK_TIMEOUT,
                    "VietShare request timed out", e);
        }
    }

    // ──────────────────────────────────────────────────────
    // placeOrder — POST /v1/orders  (stub — implement ở bước tiếp theo)
    // ──────────────────────────────────────────────────────

    @Override
    public OrderResult placeOrder(OrderRequest request) {
        log.debug("Placing order to VietShare: POST /v1/orders, productCode={}", request.productExternalCode());

        VsOrderRequest vsReq = new VsOrderRequest(
                request.productExternalCode(),
                request.quantity(),
                request.maxUnitPrice(),
                request.couponCode(),
                request.flashSaleId()
        );

        byte[] rawBody;
        try {
            rawBody = objectMapper.writeValueAsBytes(vsReq);
        } catch (Exception e) {
            throw new SupplierException(ErrorCode.INVALID_REQUEST, "Failed to serialize order request", e);
        }

        SignedHeaders headers = signer.sign("POST", "/v1/orders", rawBody);

        try {
            VsOrderResponse response = restClient.post()
                    .uri(endpoint("/v1/orders"))
                    .header("X-Shop-API-ID", headers.xShopApiId())
                    .header("X-Timestamp",   headers.xTimestamp())
                    .header("X-Nonce",       headers.xNonce())
                    .header("X-Signature",   headers.xSignature())
                    .header("Idempotency-Key", request.idempotencyKey())
                    .body(rawBody)
                    .retrieve()
                    .body(VsOrderResponse.class);

            if (response == null || !"success".equals(response.status()) || response.data() == null) {
                log.warn("VietShare returned invalid order response");
                throw new SupplierException(ErrorCode.SERVER_ERROR, "Invalid response format from VietShare");
            }

            List<String> accountDataList = response.data().deliveredAccounts().stream()
                    .map(VsDeliveredAccount::data)
                    .toList();

            return new OrderResult(
                    response.data().orderId(),
                    response.data().unitPrice(),
                    accountDataList
            );

        } catch (HttpClientErrorException e) {
            return handleClientError(e, "placeOrder");
        } catch (HttpServerErrorException e) {
            log.warn("VietShare server error during placeOrder: {}", e.getStatusCode());
            throw new SupplierException(ErrorCode.SERVER_ERROR,
                    "VietShare server error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.warn("VietShare timeout during placeOrder", e);
            throw new SupplierException(ErrorCode.NETWORK_TIMEOUT,
                    "VietShare request timed out", e);
        }
    }

    // ──────────────────────────────────────────────────────
    // Error handling helpers
    // ──────────────────────────────────────────────────────

    /** Ánh xạ HTTP 4xx sang SupplierException có ErrorCode phù hợp (ADR-006) */
    private <T> T handleClientError(HttpClientErrorException e, String operation) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String body = e.getResponseBodyAsString();
        log.warn("VietShare client error during {}: {}", operation, status);

        Duration retryAfter = parseRetryAfter(e);
        throw switch (status) {
            case UNAUTHORIZED -> new SupplierException(ErrorCode.UNAUTHORIZED,
                    "VietShare authentication failed — check API key/secret", e);
            case TOO_MANY_REQUESTS -> new SupplierException(ErrorCode.RATE_LIMITED,
                    "VietShare rate limit exceeded", e, retryAfter);
            case CONFLICT -> parseConflictError(body, e, retryAfter);
            default -> new SupplierException(ErrorCode.INVALID_REQUEST,
                    "VietShare rejected request: " + body, e);
        };
    }

    /**
     * HTTP 409 có thể là PRICE_CHANGED hoặc OUT_OF_STOCK — phân biệt qua body.
     * Xem docs/04-suppliers/vietshare.md — Bảng Mã Lỗi.
     */
    private SupplierException parseConflictError(String body, HttpClientErrorException cause,
                                                   Duration retryAfter) {
        if (body.contains("PRICE_CHANGED")) {
            return new SupplierException(ErrorCode.PRICE_CHANGED,
                    "Supplier price changed, user must confirm new price", cause);
        }
        if (body.contains("OUT_OF_STOCK")) {
            return new SupplierException(ErrorCode.OUT_OF_STOCK,
                    "Product is out of stock at supplier", cause);
        }
        if (body.contains("REQUEST_IN_PROGRESS")) {
            return new SupplierException(ErrorCode.REQUEST_IN_PROGRESS,
                    "Request is already being processed, please wait and retry", cause, retryAfter);
        }
        return new SupplierException(ErrorCode.INVALID_REQUEST,
                "Supplier returned an unrecognized conflict", cause);
    }

    private Duration parseRetryAfter(HttpClientErrorException exception) {
        if (exception.getResponseHeaders() == null) {
            return null;
        }
        String value = exception.getResponseHeaders().getFirst("Retry-After");
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            long seconds = Long.parseLong(value.trim());
            return seconds < 0 ? null : Duration.ofSeconds(seconds);
        } catch (NumberFormatException ignored) {
            try {
                Instant retryAt = ZonedDateTime.parse(value.trim(), DateTimeFormatter.RFC_1123_DATE_TIME)
                        .toInstant();
                Duration delay = Duration.between(Instant.now(), retryAt);
                return delay.isNegative() ? null : delay;
            } catch (DateTimeParseException invalidHeader) {
                log.warn("VietShare returned invalid Retry-After header");
                return null;
            }
        }
    }
}
