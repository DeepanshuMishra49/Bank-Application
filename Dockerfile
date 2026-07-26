# -----------------------------
# Stage 1 - Build
# -----------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# -----------------------------
# Stage 2 - Runtime
# -----------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create directories if needed
RUN mkdir -p /app/logs

# Copy the built JAR
COPY --from=builder /build/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# JVM settings
ENTRYPOINT ["java", \
"-Xms256m", \
"-Xmx512m", \
"-XX:+UseG1GC", \
"-jar", \
"app.jar"]
