# Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Resolve Maven dependencies before copying source files
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy application source and create the executable package
COPY src ./src
RUN mvn -q -DskipTests package

# Prepare the lightweight Java runtime image
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create a non-root user for the application
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the packaged application from the build stage
COPY --from=build /build/target/mediconnect-api-0.0.1-SNAPSHOT.jar /app/app.jar
RUN chown spring:spring /app/app.jar

USER spring:spring

# Configure the production profile and JVM runtime options
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=25.0 -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Check application health through the Actuator endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD wget -qO- http://127.0.0.1:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
