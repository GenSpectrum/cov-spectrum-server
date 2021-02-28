FROM python:3.8
WORKDIR /app/

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

# TODO The number of workers should be configurable with an environment variable
ENTRYPOINT gunicorn --workers=4 --bind=0.0.0.0:7070 wsgi:app
EXPOSE 7070
