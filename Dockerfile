# One-container deploy: nginx serves the UI on $PORT and proxies /api to the
# JVM on 127.0.0.1:8080 — same origin, so no CORS anywhere. Built for Hugging
# Face Spaces (Docker SDK), runs anywhere Docker does:
#   docker build -t sovereignty-allinone .
#   docker run -p 8090:8090 -e PORT=8090 -e COGNODB_URI=... -e COGNODB_USER=... -e COGNODB_PASSWORD=... sovereignty-allinone

# ---- frontend build ----
FROM node:22-alpine AS ui
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-fund --no-audit
COPY frontend/ .
# no VITE_API_BASE_URL on purpose: the API is same-origin behind nginx
RUN npm run build

# ---- backend build ----
FROM maven:3.9-eclipse-temurin-17 AS api
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -q dependency:go-offline
COPY backend/src src
RUN mvn -q package -DskipTests

# ---- run ----
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache nginx curl
WORKDIR /app
COPY --from=api /app/target/sovereignty-1.0.0.jar app.jar
COPY --from=ui /app/dist /srv/ui
COPY deploy/nginx.conf /app/nginx.conf
COPY deploy/start.sh /app/start.sh
RUN chmod +x /app/start.sh
# cap heap by container RAM; die on OOM so the start.sh loop gives a fresh JVM
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
EXPOSE 7860
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -sf "http://127.0.0.1:${PORT:-7860}/api/health" || exit 1
ENTRYPOINT ["/app/start.sh"]
