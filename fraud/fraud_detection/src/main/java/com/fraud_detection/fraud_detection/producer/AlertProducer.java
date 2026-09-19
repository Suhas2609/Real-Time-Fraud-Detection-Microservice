package com.fraud_detection.fraud_detection.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detection.fraud_detection.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AlertProducer {

    private static final String ALERT_TOPIC = "alerts";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publishTransaction(Transaction txn) {
        try {
            String message = objectMapper.writeValueAsString(txn);
            kafkaTemplate.send("transactions", txn.getTxnId(), message);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish transaction", e);
        }
    }

    public void publishAlert(Transaction txn) {
        try {
            String message = objectMapper.writeValueAsString(txn);
            kafkaTemplate.send(ALERT_TOPIC, txn.getTxnId(), message);
            System.out.println("📤 Alert sent for TXN ID: " + txn.getTxnId());
        } catch (Exception e) {
            System.err.println("❌ Failed to publish alert: " + e.getMessage());
        }
    }
}
