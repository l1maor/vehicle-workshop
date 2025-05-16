FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend

RUN npm install -g pnpm

COPY frontend/package.json frontend/pnpm-lock.yaml ./

ENV PNPM_HOME=/pnpm-store
RUN mkdir -p $PNPM_HOME

RUN --mount=type=cache,target=/pnpm-store \
    pnpm install --frozen-lockfile

COPY frontend/ ./

RUN pnpm build

FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app

COPY pom.xml ./

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

COPY src ./src

COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=backend-build --chown=spring:spring /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
