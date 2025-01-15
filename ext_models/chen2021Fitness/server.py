from typing import Dict
from flask import Flask, request
from main_work import process

app = Flask(__name__)


@app.route("/", methods=["POST"])
def without_prediction():
    try:
        return process(request.json)
    except Exception as e:
        return {"error": str(e)}, 500
