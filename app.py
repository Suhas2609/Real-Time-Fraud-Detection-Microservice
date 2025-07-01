from flask import Flask, jsonify, request
import numpy as np
import joblib

print("🚀 Loading model and threshold...")  # Debug line

app = Flask(__name__)

try:
    model = joblib.load("model/xgboost_model.pkl")
    with open("model/optimal_threshold.txt", "r") as f:
        THRESHOLD = float(f.read())
    print("✅ Model and threshold loaded successfully!")
except Exception as e:
    print("❌ Error loading model or threshold:", e)
    raise

@app.route("/")
def home():
    return "Fraud Detection API is running 🚀"


@app.route("/predict", methods=["POST"])

def predict():
    try:
        data = request.get_json()

        if "features" not in data:
            return jsonify({"error": "Missing 'features' in request"}), 400

        if len(data["features"]) != 29:
            return jsonify({"error": "Expected 29 features, got %d" % len(data["features"])}), 400

        #Expecting a JSON array of 30 features
        features = np.array(data["features"]).reshape(1, -1)

        # Run Model Prediction
        prob = model.predict_proba(features)[0][1]
        is_fraud = prob >= THRESHOLD

        return jsonify({
            "is_fraud": bool(is_fraud),
            "probability": float(prob),
            "threshold": float(THRESHOLD)
        })
    except Exception as e:
        app.logger.error("❌ Prediction error: %s", str(e))
        return jsonify({"error": str(e)}), 400
    
if __name__ == '__main__':
    print("🔥 Starting Flask server on http://0.0.0.0:5000")
    app.run(host='0.0.0.0', port=5000, debug=True)