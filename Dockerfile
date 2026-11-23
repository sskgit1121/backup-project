# Multi-stage Dockerfile for Spring Boot Microservices
# Stage 1: Build & Package
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

# Cache Maven wrapper and POM files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY config-server/pom.xml config-server/
COPY customer-service/pom.xml customer-service/
COPY inventory-service/pom.xml inventory-service/
COPY notification-service/pom.xml notification-service/
COPY order-service/pom.xml order-service/
COPY payment-service/pom.xml payment-service/
COPY product-service/pom.xml product-service/
COPY service-registry/pom.xml service-registry/
COPY shipping-service/pom.xml shipping-service/

RUN ./mvnw dependency:go-offline -B || true

# Copy all source files
COPY api-gateway/src api-gateway/src
COPY auth-service/src auth-service/src
COPY config-server/src config-server/src
COPY customer-service/src customer-service/src
COPY inventory-service/src inventory-service/src
COPY notification-service/src notification-service/src
COPY order-service/src order-service/src
COPY payment-service/src payment-service/src
COPY product-service/src product-service/src
COPY service-registry/src service-registry/src
COPY shipping-service/src shipping-service/src

# Argument to select which service to package
ARG SERVICE_NAME
RUN ./mvnw clean package -pl ${SERVICE_NAME} -am -DskipTests

# Stage 2: Minimal Distroless / JRE Runtime
FROM eclipse-temurin:17-jre-jammy
ARG SERVICE_NAME
WORKDIR /app

# Run as non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=builder /workspace/${SERVICE_NAME}/target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
