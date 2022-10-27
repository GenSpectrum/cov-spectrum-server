FROM python:3.10
WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
RUN chmod +x start.sh

ENV DB_HOST=""
ENV DB_PORT=""
ENV DB_NAME=""
ENV DB_USER=""
ENV DB_PASSWORD=""
ENV NOTIFICATION_KEY=""

CMD ["bash", "/app/start.sh"]
