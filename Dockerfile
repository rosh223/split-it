# Stage 1: Build the Spring Boot Application
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first for efficient layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy custom source directories
COPY src/ src/
COPY tests/ tests/

# Build the executable Spring Boot JAR file
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create local data directory for JSON file persistence
RUN mkdir -p /app/data

# Copy the built JAR artifact from Stage 1
COPY --from=build /app/target/smart-expense-tracker-api-1.0.0.jar /app/app.jar

# Expose API Port
EXPOSE 8080

# Set default environment variable for storage location
ENV STORAGE_FILE_PATH=data/expenses.json

# Launch the REST API
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
