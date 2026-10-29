# Stage 1: Build application
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for container security
RUN addgroup -S tutien && adduser -S tutien -G tutien

# Copy built artifact and dependencies
COPY --from=builder --chown=tutien:tutien /build/target/tu-tien-server-0.1.0-SNAPSHOT.jar app.jar
COPY --from=builder --chown=tutien:tutien /build/target/dependency/ ./dependency/

USER tutien

EXPOSE 19130

ENTRYPOINT ["java", "-jar", "app.jar"]
