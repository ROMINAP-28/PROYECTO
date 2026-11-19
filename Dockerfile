# ==========================================================
# STAGE 1: Build de la aplicación Spring Boot
# ==========================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# 1. Copiar wrapper y pom.xml para aprovechar cache de capas Docker
COPY .mvn .mvn
COPY mvnw mvnw.cmd pom.xml ./

# Corregir saltos de línea CRLF de Windows y dar permisos de ejecución
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Descargar dependencias en cache
RUN ./mvnw dependency:go-offline -B || true

# 2. Copiar estructura real del proyecto (Springboot/src, Springboot/resources)
COPY Springboot ./Springboot

# Compilar y empaquetar sin ejecutar tests (los tests se ejecutan en CI)
RUN ./mvnw clean package -DskipTests -B

# ==========================================================
# STAGE 2: Imagen final ligera de ejecución (JRE)
# ==========================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Crear usuario y grupo sin privilegios root
RUN groupadd -r travelink && useradd -r -g travelink travelink

# Copiar el JAR generado desde el builder
COPY --from=builder /build/target/travelink-1.0.0.jar app.jar

# Crear carpeta de logs con permisos para el usuario travelink
RUN mkdir -p /app/logs && chown -R travelink:travelink /app

USER travelink

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
