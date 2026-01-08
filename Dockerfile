# USAMOS UNA IMAGEN BASE PARA COMPILAR EL CÓDIGO (Etapa Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar pom.xml (Para que Maven use el cache de dependencias)
COPY pom.xml .

# Descargar las dependencias
RUN mvn dependency:go-offline

# Copiar el código fuente completo
COPY src ./src

# Compilar la aplicación Spring Boot y crear el JAR ejecutable
RUN mvn package -DskipTests

# -------------------------------------------------------------------

# USAMOS UNA IMAGEN BASE LIGERA PARA LA EJECUCIÓN (Etapa Runtime)
FROM eclipse-temurin:21-jre-alpine

# Copiamos el JAR compilado desde la etapa 'build'
COPY --from=build /app/target/*.jar app.jar

# Define el puerto que expone la aplicación Spring Boot
ARG PORT_BACKEND

EXPOSE ${PORT_BACKEND}

# Comando para ejecutar el JAR al iniciar el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]