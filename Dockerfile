# Backend Dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Set working directory
WORKDIR /app

# Copy only the POM file first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies (will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Second stage: minimal runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built artifact
COPY --from=build /app/target/*.jar app.jar

# Define health check
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose port
EXPOSE 8080
