# ==========================================
# ETAPA 1: BUILD (Compilación)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copiamos solo el pom.xml primero para cachear las dependencias
COPY pom.xml .

# Descargamos dependencias
RUN mvn dependency:go-offline

# 2. Copiamos el código fuente y compilamos
COPY src ./src

# Compilamos el proyecto y generamos el JAR
RUN mvn package

# ==========================================
# ETAPA 2: RUNTIME (Ejecución)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

# Instalamos fuentes y librerías gráficas para generar PDFs
RUN apk add --no-cache fontconfig ttf-dejavu

# Seguridad: Creamos un grupo y usuario limitado
RUN addgroup -S spring && adduser -S spring -G spring

# Carpetas y Permisos
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs
USER spring:spring

# Copiamos el JAR compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9001

# JAVA_OPTS:
# -XX:+UseContainerSupport: Hace que Java entienda que está en Docker
# -XX:MaxRAMPercentage=75.0: Usa el 75% de la RAM que le des al contenedor
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]