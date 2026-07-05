# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Pre-fetch dependencies (cached layer) before copying source
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/docker-command-builder.jar app.jar

# Render sets $PORT at runtime; application.properties reads it via ${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
