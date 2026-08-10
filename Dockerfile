# ──────────────────────────────────────────────────────────────────
# Stage 1 — Build
# ──────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# BUG FIX: pin a specific OTel agent version instead of 'latest'
# Using 'latest' makes builds non-reproducible and can break on new releases.
ARG OTEL_AGENT_VERSION=2.9.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar /app/agent.jar

# Cache Maven dependencies before copying source (layer-cache optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ──────────────────────────────────────────────────────────────────
# Stage 2 — Runtime
# ──────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# BUG FIX: install curl so the docker-compose healthcheck doesn't fail.
# eclipse-temurin:21-jre-alpine ships without curl; the compose healthcheck
# runs `curl -f http://localhost:8080/actuator/health` which would always
# error, keeping the container stuck in "starting" state forever.
RUN apk add --no-cache curl

RUN mkdir -p /app/logs /app/uploads

COPY --from=builder /build/target/*.jar app.jar
COPY --from=builder /app/agent.jar       agent.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-javaagent:/app/agent.jar", \
  "-jar", \
  "app.jar"]
