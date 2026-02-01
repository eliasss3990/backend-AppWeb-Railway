# ETAPA BUILD
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# ETAPA RUNTIME
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9001

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]