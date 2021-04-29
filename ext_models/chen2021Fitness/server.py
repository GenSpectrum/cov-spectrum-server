from typing import Dict
from flask import Flask, request
from main_work import process_with_prediction, process_without_prediction

app = Flask(__name__)


@app.route("/with-prediction", methods=["POST"])
def with_prediction() -> Dict:
    return process_with_prediction(request.json)


@app.route("/without-prediction", methods=["POST"])
def without_prediction() -> Dict:
    return process_without_prediction(request.json)
