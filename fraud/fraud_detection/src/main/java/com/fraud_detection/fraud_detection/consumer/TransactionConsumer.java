package com.fraud_detection.fraud_detection.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detection.fraud_detection.model.Transaction;
import com.fraud_detection.fraud_detection.service.FraudDetectionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    private final FraudDetectionService fraudDetectionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionConsumer(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @KafkaListener(topics = "transactions", groupId = "fraud-detector-group")
    public void consume(String message) {
        try {
            Transaction txn = objectMapper.readValue(message, Transaction.class);
            fraudDetectionService.processTransaction(txn);
        } catch (Exception e) {
            System.err.println("❌ Failed to consume message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}