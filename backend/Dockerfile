# Build stage
FROM eclipse-temurin:25.0.3_9-jdk-jammy@sha256:c0e0be82836e51fc78c06db5ab2b4d440f1ce1672f4c4589c9c9ec2f3f432477 AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached layer if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src/ ./src/

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25.0.3_9-jre-jammy@sha256:966f3ab99ef7ad3cbc89ebaf954597d205a5e8d6a53915280afd48fed4a59c5b

# Install curl for health check, then clean up to reduce image size
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose the application port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run the application with security flags
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
