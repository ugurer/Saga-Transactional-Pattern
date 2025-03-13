package com.example.shippingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Shipping Service uygulaması için ana sınıf.
 * Bu servis, SAGA pattern içinde sevkiyat işlemlerini yönetir.
 */
@SpringBootApplication
@EnableTransactionManagement
public class ShippingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShippingServiceApplication.class, args);
    }
} 