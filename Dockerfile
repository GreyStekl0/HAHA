# syntax=docker/dockerfile:1

# Stage 1: Build the application
FROM gradle:8.13.0-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle shadowJar --no-daemon

# Stage 2: Run the application
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/haha-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
