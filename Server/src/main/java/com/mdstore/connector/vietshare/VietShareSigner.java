package com.mdstore.connector.vietshare;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Tạo HMAC-SHA256 signature cho mọi request ra VietShare (ADR-003).
 *
 * Canonical String format (BẮT BUỘC đúng thứ tự):
 *   timestamp|nonce|METHOD|PATH_WITH_QUERY|sha256(raw_body)
 *
 * Chi tiết: docs/04-suppliers/vietshare.md — Xác Thực: HMAC-SHA256
 */
@Component
public class VietShareSigner {

    private final String apiKey;
    private final String apiSecret;

    public VietShareSigner(VietShareProperties props) {
        this.apiKey    = props.apiKey();
        this.apiSecret = props.apiSecret();
    }

    /**
     * Ký một request và trả về headers đã điền đủ giá trị.
     *
     * @param method        HTTP method, ví dụ: "GET", "POST"
     * @param pathWithQuery đường dẫn + query, ví dụ: "/v1/products" hoặc "/v1/orders?type=email"
     * @param rawBody       body bytes thô. Truyền byte[0] hoặc null nếu không có body (GET).
     *                      sha256 của empty body ≠ null, phải tính sha256("").
     * @return SignedHeaders chứa đủ 4 header để gửi lên VietShare
     */
    public SignedHeaders sign(String method, String pathWithQuery, byte[] rawBody) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce     = UUID.randomUUID().toString();

        byte[] bodyBytes  = (rawBody != null) ? rawBody : new byte[0];
        String bodyHash   = sha256Hex(bodyBytes);
        String canonical  = timestamp + "|" + nonce + "|" + method + "|" + pathWithQuery + "|" + bodyHash;
        String signature  = hmacSha256Hex(canonical, apiSecret);

        return new SignedHeaders(apiKey, timestamp, nonce, signature);
    }

    // --- Internal helpers ---

    /** SHA-256 của bytes, trả về hex lowercase */
    protected String sha256Hex(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luôn có trong JVM, không thể xảy ra
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** HMAC-SHA256 của message với key, trả về hex lowercase */
    protected String hmacSha256Hex(String message, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            byte[] result = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 signing failed", e);
        }
    }

    /**
     * Headers đã ký, sẵn sàng gắn vào HTTP request.
     * Tên header phải đúng với docs/04-suppliers/vietshare.md.
     */
    public record SignedHeaders(
            String xShopApiId,
            String xTimestamp,
            String xNonce,
            String xSignature
    ) {}
}
