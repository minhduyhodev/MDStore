package com.mdstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MDStore — Dropshipping tài khoản số tự động.
 * Virtual Threads bật qua spring.threads.virtual.enabled=true (ADR-002).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling   // cho CatalogSyncService cron job (Flow 3)
public class MdstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdstoreApplication.class, args);
    }
}
