# =============================================================================
# Stage 1 – Builder  (JDK + Maven)
# Build context = racine du monorepo ; SERVICE_NAME sélectionne le module cible
# =============================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder
ARG SERVICE_NAME

WORKDIR /workspace

RUN apk add --no-cache maven

# ── 1. Copie des pom.xml uniquement → layer cache invalidé seulement si un pom change
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

# Pré-téléchargement des dépendances (invalidé uniquement si un pom.xml change)
RUN mvn dependency:go-offline -q --no-transfer-progress 2>/dev/null || true

# ── 2. Copie des sources
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

# ── 3. Compilation : d'abord le module commun, puis le service cible
RUN mvn -pl hirehub-common clean install -DskipTests -q --no-transfer-progress && \
    mvn -pl ${SERVICE_NAME} clean package -DskipTests -q --no-transfer-progress

# =============================================================================
# Stage 2 – Runtime  (JRE seul, image minimale)
# =============================================================================
FROM eclipse-temurin:17-jre-alpine
ARG SERVICE_NAME

WORKDIR /app

RUN apk add --no-cache curl && \
    addgroup -g 1001 appgroup && \
    adduser  -D -u 1001 -G appgroup appuser

COPY --from=builder /workspace/${SERVICE_NAME}/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# Healthcheck générique (overridé par docker-compose pour chaque service)
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
