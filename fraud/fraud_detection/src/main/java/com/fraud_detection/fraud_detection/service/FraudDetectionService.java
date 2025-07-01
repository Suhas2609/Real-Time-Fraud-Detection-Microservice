package com.fraud_detection.fraud_detection.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detection.fraud_detection.model.Transaction;
import com.fraud_detection.fraud_detection.producer.AlertProducer;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class FraudDetectionService {

    private static final String FLASK_API_URL = "http://localhost:5000/predict";

    private final AlertProducer alertProducer;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public FraudDetectionService(AlertProducer alertProducer, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.alertProducer = alertProducer;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void processTransaction(Transaction txn) {
        try {
            // 1. Build JSON request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("features", txn.getFeatures());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 2. Call Flask API
            ResponseEntity<String> response = restTemplate.postForEntity(FLASK_API_URL, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                boolean isFraud = root.get("is_fraud").asBoolean();
                double prob = root.get("probability").asDouble();
                double threshold = root.get("threshold").asDouble();

                if (isFraud) {
                    System.out.println("🚨 FRAUD DETECTED!");
                    System.out.println("   ➤ TXN ID     : " + txn.getTxnId());
                    System.out.println("   ➤ Probability: " + prob);
                    System.out.println("   ➤ Threshold  : " + threshold);
                    alertProducer.publishAlert(txn); // Send to Kafka "alerts"
                } else {
                    System.out.println("✅ LEGIT TXN:");
                    System.out.println("   ➤ TXN ID     : " + txn.getTxnId());
                    System.out.println("   ➤ Probability: " + prob);
                    System.out.println("   ➤ Threshold  : " + threshold);
                }
            } else {
                System.err.println("❌ Error from Flask API: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Exception in processing transaction:");
            e.printStackTrace();
        }
    }
}
