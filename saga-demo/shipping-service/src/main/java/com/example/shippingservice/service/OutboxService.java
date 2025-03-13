package com.example.shippingservice.service;

/**
 * Outbox pattern için servis arayüzü.
 * Bu servis, veritabanı işlemleri ile mesaj gönderme işlemlerini atomik hale getirmek için kullanılır.
 */
public interface OutboxService {

    /**
     * Yeni bir olayı outbox tablosuna kaydeder.
     *
     * @param aggregateType Olayın ilişkili olduğu aggregate tipi (örn. "Shipment")
     * @param aggregateId Olayın ilişkili olduğu aggregate ID'si (örn. shipment ID)
     * @param eventType Olay tipi (örn. "SHIPMENT_CREATED")
     * @param payload Olayın JSON formatındaki içeriği
     * @return Kaydedilen outbox girdisinin ID'si
     */
    Long saveEvent(String aggregateType, String aggregateId, String eventType, String payload);

    /**
     * Outbox tablosundaki işlenmemiş olayları işler ve Kafka'ya gönderir.
     * Bu metod genellikle bir zamanlayıcı tarafından periyodik olarak çağrılır.
     */
    void processOutboxEvents();
} 