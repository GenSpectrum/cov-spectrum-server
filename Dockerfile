## Build app ##

FROM openjdk:17 AS builder
WORKDIR /build/

COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test


## Run server ##

FROM openjdk:17 AS server
WORKDIR /app

COPY --from=builder /build/build/libs /app

EXPOSE 30000
ENTRYPOINT ["java", "-Xmx3g", "-jar", "/app/cov-spectrum-0.0.1-SNAPSHOT.jar"]

