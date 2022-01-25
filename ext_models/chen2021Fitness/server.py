from typing import Dict
from flask import Flask, request
from main_work import process

app = Flask(__name__)


@app.route("/", methods=["POST"])
def without_prediction() -> Dict:
    return process(request.json)
