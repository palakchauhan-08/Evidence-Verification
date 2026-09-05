# Stage 1: Build application with Maven and JDK 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production JAR skipping unit tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime image using Eclipse Temurin JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built JAR artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
