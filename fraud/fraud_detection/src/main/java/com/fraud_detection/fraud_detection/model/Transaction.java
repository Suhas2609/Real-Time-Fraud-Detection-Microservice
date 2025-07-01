package com.fraud_detection.fraud_detection.model;

public class Transaction {
    private String txnId;
    private double[] features; // 29 features

    public Transaction() {}

    public Transaction(String txnId, double[] features) {
        this.txnId = txnId;
        this.features = features;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public double[] getFeatures() {
        return features;
    }

    public void setFeatures(double[] features) {
        this.features = features;
    }
}
