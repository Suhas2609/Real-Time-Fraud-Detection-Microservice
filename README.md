# Real-Time Fraud Detection Microservice

A complete real-time microservice pipeline for detecting fraudulent credit card transactions using a machine learning model (XGBoost), Kafka-based streaming, and a Spring Boot backend. Inspired by real-world architectures such as those at PhonePe.

---

## ✨ Project Overview

This project demonstrates a modular, real-time fraud detection architecture using:

* **Apache Kafka** for streaming transaction data.
* **Flask** (Python) to expose an XGBoost model for fraud prediction.
* **Spring Boot** (Java) to consume transactions, call the ML API, and trigger alerts.

### Workflow Summary:

1. Transaction JSONs are published to Kafka `transactions` topic.
2. Spring Boot app consumes these messages.
3. Each transaction is sent to a Flask REST API which returns fraud score.
4. If fraud is detected, the transaction is published to Kafka `alerts` topic.

---

## 🛠️ Tech Stack

| Component       | Technology Used          | Purpose                                       |
| --------------- | ------------------------ | --------------------------------------------- |
| Streaming Layer | Apache Kafka             | Real-time ingestion of transaction events     |
| ML Inference    | Flask (Python) + XGBoost | Serve fraud detection predictions via REST    |
| Backend Service | Java 21 + Spring Boot    | Orchestrate stream processing and API calls   |
| Serialization   | JSON + Jackson           | Standardized data transfer between components |
| Build Tool      | Maven                    | Java dependency and build management          |

---

## ⚖️ Running the Pipeline (For Developers & Testers)

### Prerequisites

*

### 1. Set Up Kafka Topics

Start Zookeeper and Kafka:

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# In a new terminal, start Kafka
bin/kafka-server-start.sh config/server.properties
```

Create topics:

```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 \
  --create --topic transactions --partitions 3 --replication-factor 1

bin/kafka-topics.sh --bootstrap-server localhost:9092 \
  --create --topic alerts --partitions 1 --replication-factor 1
```

---

### 2. Start the Flask ML API

```bash
python app.py
```

The model will be served at: `http://localhost:5000`

---

### 3. Start Spring Boot Microservice

```bash
cd fraud/fraud_detection
mvn clean spring-boot:run
```

You should see Spring Boot consume `transactions`, invoke the Flask API, and log LEGIT or FRAUD verdicts.

---

### 4. Send Sample Transaction

```bash
bin/kafka-console-producer.sh --topic transactions --bootstrap-server localhost:9092
```

Paste JSON (1 line):

```json
{"txnId":"txn1001","features":[0.23, -0.98, 0.45, ..., 0.91]}
```

To verify alerts:

```bash
bin/kafka-console-consumer.sh --topic alerts --bootstrap-server localhost:9092 --from-beginning
```

---

## 🌐 Sample Output

```bash
🚗 LEGIT TXN:
   ➔ TXN ID     : txn1001
   ➔ Probability: 0.34
   ➔ Threshold  : 0.56
```

```bash
🚨 FRAUD DETECTED!
   ➔ TXN ID     : txn1002
   ➔ Probability: 0.91
   ➔ Threshold  : 0.56
```

---

## 📊 Future Enhancements (Detailed)

| Area                 | Description                                                          |
| -------------------- | -------------------------------------------------------------------- |
| Testing              | Add JUnit & Mockito for all Spring Boot services                     |
| Dockerization        | Add Dockerfiles for Flask + Spring Boot and use `docker-compose.yml` |
| Monitoring           | Integrate Prometheus for metrics and Grafana dashboards              |
| CI/CD                | Add GitHub Actions: Build, Test, Docker Publish                      |
| Schema Registry      | Use Confluent Schema Registry + Avro for message format enforcement  |
| Database Integration | Store flagged frauds in PostgreSQL for audits                        |
| Security             | Secure Kafka and REST APIs with authentication & TLS                 |
| Kubernetes Support   | Add Helm Charts & manifests for deploying to a cluster               |

---

## 🚀 Status

Core functionality complete. Next up: Dockerization and JUnit testing.

---

Developed by Suhas N.
