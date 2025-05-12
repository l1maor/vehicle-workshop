# Build stage for frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend

# Install pnpm globally
RUN npm install -g pnpm

# Copy package files for dependency caching
COPY frontend/package.json frontend/pnpm-lock.yaml ./

# Enable pnpm store cache across builds
ENV PNPM_HOME=/pnpm-store
RUN mkdir -p $PNPM_HOME

# Install dependencies with caching
RUN --mount=type=cache,target=/pnpm-store \
    pnpm install --frozen-lockfile

# Copy frontend source files
COPY frontend/ ./

# Build frontend
RUN pnpm build

# Backend build stage
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app

# Copy POM file for dependency resolution
COPY pom.xml ./

# Cache Maven dependencies using Docker buildkit cache
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

# Copy backend source code
COPY src ./src

# Copy frontend build files to Spring static resources
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build application with Maven cache
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

# Final runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from the build stage
COPY --from=backend-build --chown=spring:spring /app/target/*.jar app.jar

# Application environment
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
