# Use official OpenJDK 21 image as base
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Install Maven and build the application
RUN apk add --no-cache maven git bash \
    && mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","target/prices-service-1.0.0.jar"]
