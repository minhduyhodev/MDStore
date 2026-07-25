package com.mdstore.connector.vietshare;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình kết nối VietShare — đọc từ application.yml prefix "vietshare".
 * Xem docs/04-suppliers/vietshare.md — Biến Môi Trường Cần Cấu Hình.
 */
@ConfigurationProperties(prefix = "vietshare")
public record VietShareProperties(
        String apiUrl,
        String apiKey,
        /** API Secret — dùng làm HMAC key (ADR-003). Không được log hay expose ra ngoài. */
        String apiSecret,
        int connectTimeoutSeconds,
        int readTimeoutSeconds
) {}
