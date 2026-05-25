# =========================
# Build stage
# =========================
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle/
COPY settings.gradle .
COPY backend/build.gradle.kts backend/


RUN chmod +x gradlew
RUN ./gradlew :backend:dependencies --no-daemon
COPY backend/src backend/src

# Build bootJar
RUN ./gradlew :backend:bootJar --no-daemon

# =========================
# Debug stage
# =========================
FROM eclipse-temurin:25-jdk AS debug
WORKDIR /app

RUN useradd -m appuser
USER appuser

ENV SPRING_PROFILES_ACTIVE=docker

COPY --from=build /workspace/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# =========================
# Production stage
# =========================
FROM gcr.io/distroless/java25-debian13:nonroot AS prod

WORKDIR /app

USER 65532

ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /workspace/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]