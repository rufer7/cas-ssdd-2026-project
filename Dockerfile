# =========================
# Frontend build stage
# Builds the Vite single-page app that the backend serves as static content.
# =========================
FROM node:22-alpine AS frontend
WORKDIR /frontend

COPY frontend/package.json ./
RUN npm install --no-audit --no-fund

COPY frontend/ ./

# Build-time SPA configuration. Auth0 SPA settings are public (not secrets); the
# API base URL is empty so the app calls the same origin that serves it.
ARG VITE_AUTH_MODE=auth0
ARG VITE_API_BASE_URL=
ARG VITE_AUTH0_DOMAIN
ARG VITE_AUTH0_CLIENT_ID
ARG VITE_AUTH0_AUDIENCE
ENV VITE_AUTH_MODE=${VITE_AUTH_MODE} \
    VITE_API_BASE_URL=${VITE_API_BASE_URL} \
    VITE_AUTH0_DOMAIN=${VITE_AUTH0_DOMAIN} \
    VITE_AUTH0_CLIENT_ID=${VITE_AUTH0_CLIENT_ID} \
    VITE_AUTH0_AUDIENCE=${VITE_AUTH0_AUDIENCE}
RUN npm run build

# =========================
# Backend build stage
# =========================
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle/
COPY settings.gradle .
COPY backend/build.gradle.kts backend/


RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew :backend:dependencies --no-daemon
COPY backend/src backend/src

# Bundle the built SPA into the backend's static resources so bootJar serves it.
COPY --from=frontend /frontend/dist backend/src/main/resources/static

# Build bootJar
RUN ./gradlew :backend:clean :backend:bootJar --no-daemon

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
