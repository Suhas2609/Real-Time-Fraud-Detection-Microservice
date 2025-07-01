package com.fraud_detection.fraud_detection.model;

public class Alert {
    private String txnId;
    private boolean isFraud;
    private double probability;

    public Alert() {}

    public Alert(String txnId, boolean isFraud, double probability) {
        this.txnId = txnId;
        this.isFraud = isFraud;
        this.probability = probability;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public boolean isFraud() {
        return isFraud;
    }

    public void setFraud(boolean fraud) {
        isFraud = fraud;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
