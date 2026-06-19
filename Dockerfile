# Builder stage
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia exacta del JAR (¡cambia el nombre si es diferente!)
COPY --from=builder /workspace/target/sistema-inscripcion-cursos-0.0.1-SNAPSHOT.jar /app/app.jar

# Verificación de que el JAR existe
RUN ls -lh /app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
