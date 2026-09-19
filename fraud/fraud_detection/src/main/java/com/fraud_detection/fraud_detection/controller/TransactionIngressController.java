package com.fraud_detection.fraud_detection.controller;

import com.fraud_detection.fraud_detection.model.Transaction;
import com.fraud_detection.fraud_detection.producer.AlertProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionIngressController {

    private final AlertProducer alertProducer;

    public TransactionIngressController(AlertProducer alertProducer) {
        this.alertProducer = alertProducer;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> publish(@RequestBody Transaction transaction) {
        if (transaction.getTxnId() == null || transaction.getTxnId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "txnId is required"));
        }
        if (transaction.getFeatures() == null || transaction.getFeatures().length != 29) {
            return ResponseEntity.badRequest().body(Map.of("error", "Expected exactly 29 features"));
        }

        alertProducer.publishTransaction(transaction);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "txnId", transaction.getTxnId(),
                "topic", "transactions",
                "published", true
        ));
    }
}