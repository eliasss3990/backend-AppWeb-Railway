# -------------------------------------------------------------------
# ETAPA 1: BUILD
# -------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos pom y descargamos dependencias (Capa cacheada)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos código y compilamos
COPY src ./src
RUN mvn package -DskipTests

# -------------------------------------------------------------------
# ETAPA 2: RUNTIME (PRODUCCIÓN)
# -------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

# SEGURIDAD: Crear usuario 'spring' para no usar root
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio de logs y asignar permisos
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

# Cambiar al usuario seguro
USER spring:spring

# Copiar el JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Informamos el puerto (documentación)
EXPOSE 9001

# EJECUCIÓN:
# -XX:MaxRAMPercentage=75.0 -> Usa el 75% de la memoria del contenedor (evita OOM kills)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]