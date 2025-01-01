import logging
from logging.handlers import RotatingFileHandler
from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

# Configure logging
# logging.basicConfig(level=logging.DEBUG)

# Configure file logging
log_file = "flask_app.log"
file_handler = RotatingFileHandler(
    log_file, maxBytes=1024 * 1024 * 10, backupCount=5  # 10MB per file, 5 backups
)
file_handler.setLevel(logging.DEBUG)
file_handler.setFormatter(
    logging.Formatter(
        "%(asctime)s %(levelname)s: %(message)s [in %(pathname)s:%(lineno)d]"
    )
)
app.logger.addHandler(file_handler)

# Also log to console
console_handler = logging.StreamHandler()
console_handler.setLevel(logging.DEBUG)
console_handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s: %(message)s"))
app.logger.addHandler(console_handler)

# Constants for API URLs
OPENAI_API_URL = "https://api.openai.com/v1"
LM_STUDIO_API_URL = "http://127.0.0.1:1234/v1"

# Uncomment to hard code
# OPENAI_API_KEY = ""

@app.route('/v1/<path:endpoint>', methods=['GET', 'POST'])
def proxy_request(endpoint):
    # Log the incoming request
    app.logger.debug(f"Incoming request to: {endpoint}")
    app.logger.debug(f"Request Headers: {request.headers}")
    app.logger.debug(f"Request Body: {request.get_data(as_text=True)}")

    headers = {key: value for key, value in request.headers if key.lower() != "host"}
    # headers = {"Authorization": f"Bearer {OPENAI_API_KEY}"}  # Uncomment to hard code
    response = None

    try:
        if request.is_json:
            payload = request.get_json()
            model = payload.get("model", "").lower()

            if model.startswith(("o1-", "o3-")):
                messages = payload.get("messages", [])
                filtered_messages = [
                    msg for msg in messages if msg.get("role") != "system"
                ]
                payload["messages"] = filtered_messages
                app.logger.debug(f"Filtered Messages: {filtered_messages}")

            if model.startswith(("gpt-", "o1-", "o3-")):
                target_url = f"{OPENAI_API_URL}/{endpoint}"
            else:
                target_url = f"{LM_STUDIO_API_URL}/{endpoint}"

            app.logger.debug(f"Routing request to: {target_url}")

            if request.method == "POST":
                response = requests.post(target_url, json=payload, headers=headers)
            elif request.method == "GET":
                response = requests.get(target_url, headers=headers)

            app.logger.debug(f"External Response Status Code: {response.status_code}")
            app.logger.debug(f"External Response Body: {response.text}")

            if 'application/json' in response.headers.get('Content-Type', ''):
                return jsonify(response.json()), response.status_code
            else:
                return response.text, response.status_code
        else:
            return jsonify({"error": "Request payload must be JSON"}), 400
    except Exception as e:
        app.logger.error(f"Error during forwarding: {e}")
        return jsonify({"error": "Error forwarding request"}), 500

if __name__ == '__main__':
    app.run(debug=False, host="192.168.86.55", port=1234)
