import json
from pathlib import Path

from flask import Flask, jsonify, render_template, request
import numpy as np
import joblib
from urllib.error import URLError
from urllib.request import Request, urlopen

print("🚀 Loading model and threshold...")  # Debug line

app = Flask(__name__)

try:
    model = joblib.load("model/xgboost_model.pkl")
    amount_scaler = joblib.load("model/scaler_amount.pkl")
    with open("model/optimal_threshold.txt", "r") as f:
        THRESHOLD = float(f.read())
    print("✅ Model, scaler, and threshold loaded successfully!")
except Exception as e:
    print("❌ Error loading model, scaler, or threshold:", e)
    raise

FEATURE_COUNT = 29
SPRING_INGEST_URL = "http://localhost:8080/api/transactions"
DEMO_FILE = Path(__file__).parent / "demo" / "demo_transactions.json"


def validate_features(features):
    if not isinstance(features, list):
        raise ValueError("'features' must be a JSON array")
    if len(features) != FEATURE_COUNT:
        raise ValueError(f"Expected {FEATURE_COUNT} features, got {len(features)}")
    try:
        return [float(value) for value in features]
    except (TypeError, ValueError) as error:
        raise ValueError("All features must be numeric") from error


def predict_model_features(features):
    model_features = np.asarray(validate_features(features), dtype=float).reshape(1, -1)
    probability = float(model.predict_proba(model_features)[0][1])
    return {
        "is_fraud": probability >= THRESHOLD,
        "probability": probability,
        "threshold": THRESHOLD,
    }


def prepare_browser_features(features):
    raw_features = np.asarray(validate_features(features), dtype=float)
    raw_features[28] = amount_scaler.transform([[raw_features[28]]])[0][0]
    return raw_features.tolist()

@app.route("/")
def home():
    with DEMO_FILE.open(encoding="utf-8") as demo_file:
        demo_transactions = json.load(demo_file)
    return render_template("index.html", demo_transactions=demo_transactions)


@app.route("/predict", methods=["POST"])

def predict():
    try:
        data = request.get_json()

        if not isinstance(data, dict) or "features" not in data:
            return jsonify({"error": "Missing 'features' in request"}), 400

        return jsonify(predict_model_features(data["features"]))
    except Exception as e:
        app.logger.error("❌ Prediction error: %s", str(e))
        return jsonify({"error": str(e)}), 400


@app.route("/api/demo/predict", methods=["POST"])
def demo_predict():
    try:
        data = request.get_json()
        if not isinstance(data, dict) or "features" not in data:
            return jsonify({"error": "Missing 'features' in request"}), 400

        txn_id = str(data.get("txnId", "demo-transaction")).strip()
        if not txn_id:
            return jsonify({"error": "Transaction ID cannot be empty"}), 400

        model_features = prepare_browser_features(data["features"])
        result = predict_model_features(model_features)
        kafka_request = Request(
            SPRING_INGEST_URL,
            data=json.dumps({"txnId": txn_id, "features": model_features}).encode(),
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        try:
            with urlopen(kafka_request, timeout=3) as response:
                result.update({"txnId": txn_id, "kafkaPublished": response.status == 202})
        except URLError as error:
            result.update({"txnId": txn_id, "kafkaPublished": False, "kafkaError": str(error.reason)})
        return jsonify(result)
    except Exception as e:
        app.logger.error("❌ Demo prediction error: %s", str(e))
        return jsonify({"error": str(e)}), 400
    
if __name__ == '__main__':
    print("🔥 Starting Flask server on http://0.0.0.0:5000")
    app.run(host='0.0.0.0', port=5000, debug=True)