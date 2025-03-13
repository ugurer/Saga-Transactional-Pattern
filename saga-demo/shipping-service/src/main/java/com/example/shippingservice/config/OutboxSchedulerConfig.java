package com.example.shippingservice.config;

import com.example.shippingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Outbox olaylarının periyodik olarak işlenmesi için zamanlayıcı yapılandırması.
 * Bu sınıf, Outbox tablosundaki olayları düzenli aralıklarla işleyerek Kafka'ya gönderir.
 * 
 * Not: Gerçek bir uygulamada, Debezium gibi CDC (Change Data Capture) araçları kullanılarak
 * bu işlem daha verimli bir şekilde gerçekleştirilebilir.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OutboxSchedulerConfig {

    private final OutboxService outboxService;

    /**
     * Outbox olaylarını her 10 saniyede bir işler.
     * Bu metod, OutboxService'i kullanarak işlenmemiş olayları bulur ve Kafka'ya gönderir.
     */
    @Scheduled(fixedRate = 10000) // 10 saniyede bir çalıştır
    public void processOutboxEvents() {
        log.debug("Outbox olayları zamanlayıcısı çalışıyor");
        try {
            outboxService.processOutboxEvents();
        } catch (Exception e) {
            log.error("Outbox olayları işlenirken hata oluştu: {}", e.getMessage(), e);
        }
    }
} 