# Stage 1 - Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/agent.jar

COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 - Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/logs

COPY --from=builder /build/target/*.jar app.jar
# ← Yeh line missing thi — agent copy karo builder se
COPY --from=builder /app/agent.jar agent.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-javaagent:/app/agent.jar", \
  "-jar", \
  "app.jar"]
