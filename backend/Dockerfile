# ---- build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q package -DskipTests

# ---- run ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists \
    && useradd --system --no-create-home app
COPY --from=build /app/target/sovereignty-1.0.0.jar app.jar
USER app
EXPOSE 8080
# respect the container's memory limit instead of the host's
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -sf "http://localhost:${PORT:-8080}/actuator/health" || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
