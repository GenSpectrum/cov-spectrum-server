package ch.ethz.covspectrum.chat

class LapisRequestFailedException(
    endpoint: String,
    status: Int):
    Exception("HTTP Request to $endpoint failed with status code $status")
