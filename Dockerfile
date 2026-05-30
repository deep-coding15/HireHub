# =============================================================================
# Stage 1 – Builder  (image officielle Maven 3.9 + JDK 17 – pas d'apk add maven)
#
# ARG SERVICE_NAME est déclaré le plus tard possible : tout ce qui précède
# est mis en cache Docker et PARTAGÉ entre les 11 services.
# Résultat : dependency:go-offline et la compilation de hirehub-common ne
# s'exécutent qu'une seule fois, peu importe combien de services on build.
# =============================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /workspace

# ── 1. Poms seuls (layer invalidée uniquement si un pom.xml change) ───────────
COPY pom.xml .
COPY hirehub-common/pom.xml          hirehub-common/pom.xml
COPY eureka-server/pom.xml           eureka-server/pom.xml
COPY config-server/pom.xml           config-server/pom.xml
COPY api-gateway/pom.xml             api-gateway/pom.xml
COPY auth-service/pom.xml            auth-service/pom.xml
COPY verification-service/pom.xml    verification-service/pom.xml
COPY offre-service/pom.xml           offre-service/pom.xml
COPY candidature-service/pom.xml     candidature-service/pom.xml
COPY event-service/pom.xml           event-service/pom.xml
COPY email-service/pom.xml           email-service/pom.xml
COPY entretien-service/pom.xml       entretien-service/pom.xml
COPY frontend-service/pom.xml        frontend-service/pom.xml

# ── 2. Pré-téléchargement des dépendances (PARTAGÉ entre tous les services) ──
#    Retry 5x, TTL 25s sur les connexions, pas de pool HTTP pour éviter les
#    SSL resets ("Remote host terminated the handshake").
RUN mvn dependency:go-offline -q --no-transfer-progress \
      -Dmaven.wagon.http.retryHandler.count=5 \
      -Dmaven.wagon.httpconnectionManager.ttlSeconds=25 \
      -Dmaven.wagon.http.pool=false \
      -Daether.connector.http.retryHandler.count=5 \
      2>/dev/null || true

# ── 3. Sources complètes (PARTAGÉ entre tous les services) ───────────────────
COPY hirehub-common/        hirehub-common/
COPY eureka-server/         eureka-server/
COPY config-server/         config-server/
COPY api-gateway/           api-gateway/
COPY auth-service/          auth-service/
COPY verification-service/  verification-service/
COPY offre-service/         offre-service/
COPY candidature-service/   candidature-service/
COPY event-service/         event-service/
COPY email-service/         email-service/
COPY entretien-service/     entretien-service/
COPY frontend-service/      frontend-service/

# ── 4a. Install du parent pom d'abord ─────────────────────────────────────────
RUN mvn install -DskipTests -q --no-transfer-progress \
      -Dmaven.wagon.http.retryHandler.count=5 \
      -Dmaven.wagon.httpconnectionManager.ttlSeconds=25 \
      -Dmaven.wagon.http.pool=false \
      -Daether.connector.http.retryHandler.count=5

# ── 4b. Build du module commun (PARTAGÉ – exécuté une seule fois) ──────────────
RUN mvn -pl hirehub-common clean install -DskipTests -q --no-transfer-progress \
      -Dmaven.wagon.http.retryHandler.count=5 \
      -Dmaven.wagon.httpconnectionManager.ttlSeconds=25 \
      -Dmaven.wagon.http.pool=false \
      -Daether.connector.http.retryHandler.count=5

# ── 5. Build du service cible (SPÉCIFIQUE au service) ────────────────────────
#    ARG déclaré ici → tout ce qui précède est en cache et partagé !
ARG SERVICE_NAME
RUN mvn -pl ${SERVICE_NAME} clean package -DskipTests -q --no-transfer-progress \
      -Dmaven.wagon.http.retryHandler.count=5 \
      -Dmaven.wagon.httpconnectionManager.ttlSeconds=25 \
      -Dmaven.wagon.http.pool=false \
      -Daether.connector.http.retryHandler.count=5

# =============================================================================
# Stage 2 – Runtime  (JRE seul, image minimale)
# =============================================================================
FROM eclipse-temurin:17-jre-alpine
ARG SERVICE_NAME
ARG SERVER_PORT

WORKDIR /app

RUN apk add --no-cache curl && \
    addgroup -g 1001 appgroup && \
    adduser  -D -u 1001 -G appgroup appuser

COPY --from=builder /workspace/${SERVICE_NAME}/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE ${SERVER_PORT}

# Healthcheck générique (overridé par docker-compose pour chaque service)
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["java", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]