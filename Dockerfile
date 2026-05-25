# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Gradle wrapper and build configuration first to leverage Docker caching for dependencies
COPY gradlew gradlew.bat settings.gradle ./
COPY gradle ./gradle
COPY backend/build.gradle.kts backend/settings.gradle.kts ./backend/

RUN chmod +x ./gradlew

COPY backend/src ./backend/src

RUN ./gradlew :backend:bootJar --no-daemon

FROM eclipse-temurin:25-jdk AS runtime
WORKDIR /app

COPY --from=build /app/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
